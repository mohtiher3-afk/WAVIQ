package com.example.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.example.model.AudioTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Low-level audio I/O & DSP engine built around Android [AudioTrack] and [AudioRecord].
 * Handles local PCM/WAV playback, optional live microphone capture, and a pluggable
 * Float32 block [DspHook] chain.
 */
class AudioProcessor(
    private val scope: CoroutineScope,
    val sampleRate: Int = 48000,
    private val blockSize: Int = 1024
) {
    private val hooks = CopyOnWriteArrayList<DspHook>()
    private val crossfadeSamples = (0.015f * sampleRate).toInt().coerceAtLeast(1)

    @Volatile
    private var leftPcm: FloatArray = FloatArray(0)
    @Volatile
    private var rightPcm: FloatArray = FloatArray(0)
    @Volatile
    private var playCursor: Int = 0
    @Volatile
    private var isPlayingFlag: Boolean = false
    @Volatile
    private var isRecordingFlag: Boolean = false

    @Volatile
    private var targetWetGain: Float = 1f
    private var currentWetMix: Float = 1f

    private var playJob: Job? = null
    private var recordJob: Job? = null

    private val recordedSamples = ArrayList<Float>()
    private val recordLock = Any()

    private val perHookEmaMs = ConcurrentHashMap<String, Float>()
    @Volatile
    private var smoothedChainLatencyMs: Float = 0f
    private var blocksSinceSnapshotPublish: Int = 0

    private val _telemetry = MutableStateFlow(AudioTelemetry())
    val telemetry: StateFlow<AudioTelemetry> = _telemetry.asStateFlow()

    private val _hookSnapshots = MutableStateFlow<List<DspHookSnapshot>>(emptyList())
    val hookSnapshots: StateFlow<List<DspHookSnapshot>> = _hookSnapshots.asStateFlow()

    fun registerHook(hook: DspHook) {
        if (hooks.none { it.id == hook.id }) {
            hooks.add(hook)
            refreshLatencyTelemetry()
        }
    }

    fun unregisterHook(hookId: String) {
        hooks.removeAll { it.id == hookId }
        perHookEmaMs.remove(hookId)
        refreshLatencyTelemetry()
    }

    fun setHookEnabled(hookId: String, enabled: Boolean) {
        hooks.firstOrNull { it.id == hookId }?.let {
            it.isEnabled = enabled
            if (!enabled) perHookEmaMs[hookId] = 0f
            refreshLatencyTelemetry()
        }
    }

    fun clearHooks() {
        hooks.clear()
        perHookEmaMs.clear()
        smoothedChainLatencyMs = 0f
        refreshLatencyTelemetry()
    }

    fun setWetEnabled(enabled: Boolean) {
        targetWetGain = if (enabled) 1f else 0f
        refreshLatencyTelemetry()
    }

    fun isWetEnabled(): Boolean = targetWetGain > 0.01f

    fun refreshLatencyTelemetry() {
        val bypassed = targetWetGain <= 0.01f
        val effectiveLatency = when {
            bypassed -> 0f
            isPlayingFlag && smoothedChainLatencyMs > 0f -> smoothedChainLatencyMs
            else -> computeIdleBaselineLatencyMs()
        }
        _telemetry.value = _telemetry.value.copy(
            dspLatencyMs = effectiveLatency,
            isBypassed = bypassed
        )
        publishHookSnapshots()
    }

    fun loadBuffers(left: FloatArray, right: FloatArray, infoDurationMs: Long? = null, lufs: Float = -16f, peakDb: Float = -2f) {
        leftPcm = left
        rightPcm = right
        playCursor = 0
        hooks.forEach { it.reset() }
        val duration = infoDurationMs ?: ((left.size.toLong() * 1000L) / sampleRate)
        _telemetry.value = _telemetry.value.copy(
            durationMs = duration,
            positionMs = 0L,
            integratedLufs = lufs,
            truePeakDb = peakDb,
            effectiveSampleRate = sampleRate
        )
    }

    /**
     * Parses a local 16-bit LE PCM file (with optional RIFF WAV header) into Float32 stereo buffers
     * ready for [AudioTrack] playback.
     */
    fun loadLocalPcmFile(file: File, fallbackSampleRate: Int = 48000, fallbackChannels: Int = 2): DecodedAudio {
        val bytes = file.readBytes()
        val parsed = parsePcmBytes(bytes, fallbackSampleRate, fallbackChannels)
        val info = AudioDecoder.analyzeBuffer(
            id = "pcm_${file.name}",
            title = file.name,
            sourceLabel = "LOCAL PCM FILE",
            left = parsed.first,
            right = parsed.second,
            sampleRate = sampleRate,
            channels = 2,
            isBundled = false
        )
        loadBuffers(parsed.first, parsed.second, info.durationMs, info.integratedLufs, info.truePeakDb)
        return DecodedAudio(parsed.first, parsed.second, sampleRate, info)
    }

    fun processThroughHooks(left: FloatArray, right: FloatArray, frameCount: Int) {
        var totalNs = 0L
        var anyActive = false
        for (hook in hooks) {
            if (hook.isEnabled) {
                anyActive = true
                val startNs = System.nanoTime()
                hook.processBlock(left, right, frameCount, sampleRate)
                val elapsedNs = (System.nanoTime() - startNs).coerceAtLeast(1L)
                totalNs += elapsedNs

                val rawHookMs = (elapsedNs / 1_000_000f).coerceAtLeast(0.001f)
                val prevHook = perHookEmaMs[hook.id] ?: 0f
                perHookEmaMs[hook.id] = if (prevHook <= 0f) {
                    rawHookMs
                } else {
                    prevHook * (1f - LATENCY_EMA_ALPHA) + rawHookMs * LATENCY_EMA_ALPHA
                }
            } else {
                perHookEmaMs[hook.id] = 0f
            }
        }

        val blockLatencyMs = if (anyActive) (totalNs / 1_000_000f).coerceAtLeast(0.001f) else 0f
        smoothedChainLatencyMs = when {
            !anyActive -> 0f
            smoothedChainLatencyMs <= 0f -> blockLatencyMs
            else -> smoothedChainLatencyMs * (1f - LATENCY_EMA_ALPHA) + blockLatencyMs * LATENCY_EMA_ALPHA
        }

        val bypassed = targetWetGain <= 0.01f
        _telemetry.value = _telemetry.value.copy(
            dspLatencyMs = if (bypassed) 0f else smoothedChainLatencyMs,
            isBypassed = bypassed
        )

        blocksSinceSnapshotPublish++
        if (blocksSinceSnapshotPublish >= 8 || !isPlayingFlag) {
            blocksSinceSnapshotPublish = 0
            publishHookSnapshots()
        }
    }

    fun play() {
        if (isPlayingFlag || leftPcm.isEmpty()) return
        isPlayingFlag = true
        _telemetry.value = _telemetry.value.copy(isPlaying = true)

        playJob = scope.launch(Dispatchers.Default) {
            val minBufBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(blockSize * 4)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            val dryL = FloatArray(blockSize)
            val dryR = FloatArray(blockSize)
            val wetL = FloatArray(blockSize)
            val wetR = FloatArray(blockSize)
            val pcmOut = ShortArray(blockSize * 2)
            val monoAnalysis = FloatArray(256)
            val scopePreview = FloatArray(96)

            runCatching { audioTrack.play() }

            try {
                while (isActive && isPlayingFlag) {
                    val total = leftPcm.size
                    if (total == 0) break

                    for (i in 0 until blockSize) {
                        val idx = (playCursor + i) % total
                        val dl = leftPcm[idx]
                        val dr = rightPcm[idx]
                        dryL[i] = dl
                        dryR[i] = dr
                        wetL[i] = dl
                        wetR[i] = dr
                    }

                    // Run real-time pluggable Float32 DSP hook chain in place
                    processThroughHooks(wetL, wetR, blockSize)

                    var sumSqL = 0f
                    var sumSqR = 0f
                    var peakBlock = 1e-6f
                    var midEnergy = 0f
                    var sideEnergy = 0f

                    for (i in 0 until blockSize) {
                        val step = 1f / crossfadeSamples.toFloat()
                        if (currentWetMix < targetWetGain) {
                            currentWetMix = (currentWetMix + step).coerceAtMost(1f)
                        } else if (currentWetMix > targetWetGain) {
                            currentWetMix = (currentWetMix - step).coerceAtLeast(0f)
                        }
                        val theta = (currentWetMix * PI * 0.5).toFloat()
                        val dGain = cos(theta)
                        val wGain = sin(theta)

                        val outL = (dryL[i] * dGain + wetL[i] * wGain).coerceIn(-1f, 1f)
                        val outR = (dryR[i] * dGain + wetR[i] * wGain).coerceIn(-1f, 1f)

                        pcmOut[i * 2] = (outL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                        pcmOut[i * 2 + 1] = (outR * 32767f).toInt().coerceIn(-32768, 32767).toShort()

                        sumSqL += outL * outL
                        sumSqR += outR * outR
                        val p = max(abs(outL), abs(outR))
                        if (p > peakBlock) peakBlock = p

                        val m = 0.5f * (outL + outR)
                        val s = 0.5f * (outL - outR)
                        midEnergy += m * m
                        sideEnergy += s * s

                        if (i < 256) monoAnalysis[i] = m
                        if (i < 96) scopePreview[i] = m
                    }

                    playCursor = (playCursor + blockSize) % total
                    audioTrack.write(pcmOut, 0, pcmOut.size)

                    emitTelemetryBlock(
                        monoAnalysis = monoAnalysis,
                        scopePreview = scopePreview,
                        sumSqL = sumSqL,
                        sumSqR = sumSqR,
                        peakBlock = peakBlock,
                        midEnergy = midEnergy,
                        sideEnergy = sideEnergy,
                        frameCount = blockSize,
                        positionMs = (playCursor.toLong() * 1000L) / sampleRate,
                        durationMs = (total.toLong() * 1000L) / sampleRate,
                        isPlaying = true
                    )
                }
            } finally {
                runCatching {
                    audioTrack.stop()
                    audioTrack.release()
                }
            }
        }
    }

    fun pause() {
        isPlayingFlag = false
        playJob?.cancel()
        playJob = null
        smoothedChainLatencyMs = 0f
        perHookEmaMs.clear()
        val bypassed = targetWetGain <= 0.01f
        _telemetry.value = _telemetry.value.copy(
            isPlaying = false,
            spectrumBins = List(64) { 0.04f },
            glyphZones = List(5) { 0.05f },
            dspLatencyMs = if (bypassed) 0f else computeIdleBaselineLatencyMs(),
            isBypassed = bypassed
        )
        publishHookSnapshots()
    }

    fun togglePlayPause() {
        if (isPlayingFlag) pause() else play()
    }

    fun seekToFraction(fraction: Float) {
        val total = leftPcm.size
        if (total == 0) return
        playCursor = (fraction.coerceIn(0f, 0.99f) * total).toInt()
        _telemetry.value = _telemetry.value.copy(
            positionMs = (playCursor.toLong() * 1000L) / sampleRate
        )
    }

    /**
     * Permission-guarded [AudioRecord] mic capture entry point.
     */
    fun startRecording(
        context: Context,
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasRecordAudioPermission(context)) {
            onError("RECORD_AUDIO permission not granted.")
            return
        }
        startRecording(onMeterUpdate, onError)
    }

    /**
     * Captures live PCM input via [AudioRecord] when RECORD_AUDIO permission is granted.
     * Emits live FFT & input level telemetry during capture.
     */
    @SuppressLint("MissingPermission")
    fun startRecording(
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRecordingFlag) return
        pause()

        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(blockSize * 2)

        val recorder = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2
            )
        }.getOrElse { err ->
            onError(err.message ?: "AudioRecord initialization failed.")
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            onError("AudioRecord hardware unavailable.")
            return
        }

        synchronized(recordLock) {
            recordedSamples.clear()
        }

        isRecordingFlag = true
        recordJob = scope.launch(Dispatchers.IO) {
            val readBuf = ShortArray(blockSize)
            val floatL = FloatArray(blockSize)
            val floatR = FloatArray(blockSize)
            val monoAnalysis = FloatArray(256)
            val scopePreview = FloatArray(96)
            val maxRecordSamples = sampleRate * 60 // Up to 60s capture buffer

            try {
                recorder.startRecording()
                while (isActive && isRecordingFlag) {
                    val readCount = recorder.read(readBuf, 0, readBuf.size)
                    if (readCount <= 0) continue

                    var sumSq = 0f
                    var peak = 1e-6f
                    for (i in 0 until readCount) {
                        val s = readBuf[i].toFloat() / 32768f
                        floatL[i] = s
                        floatR[i] = s
                        sumSq += s * s
                        val a = abs(s)
                        if (a > peak) peak = a
                        if (i < 256) monoAnalysis[i] = s
                        if (i < 96) scopePreview[i] = s
                    }

                    // Preview through DSP hooks for live telemetry consistency
                    processThroughHooks(floatL, floatR, readCount)

                    var totalCaptured: Int
                    synchronized(recordLock) {
                        if (recordedSamples.size + readCount <= maxRecordSamples) {
                            for (i in 0 until readCount) {
                                recordedSamples.add(floatL[i])
                            }
                        }
                        totalCaptured = recordedSamples.size
                    }

                    val rms = sqrt((sumSq / readCount).coerceAtLeast(1e-12f))
                    val durMs = (totalCaptured.toLong() * 1000L) / sampleRate
                    onMeterUpdate(rms.coerceIn(0f, 1f), durMs)

                    emitTelemetryBlock(
                        monoAnalysis = monoAnalysis,
                        scopePreview = scopePreview,
                        sumSqL = sumSq,
                        sumSqR = sumSq,
                        peakBlock = peak,
                        midEnergy = sumSq,
                        sideEnergy = 0f,
                        frameCount = readCount,
                        positionMs = durMs,
                        durationMs = durMs,
                        isPlaying = false
                    )
                }
            } catch (sec: SecurityException) {
                onError(sec.message ?: "RECORD_AUDIO permission required.")
            } finally {
                runCatching {
                    recorder.stop()
                    recorder.release()
                }
                isRecordingFlag = false
            }
        }
    }

    fun stopRecording(): DecodedAudio? {
        isRecordingFlag = false
        recordJob?.cancel()
        recordJob = null

        val captured: FloatArray
        synchronized(recordLock) {
            if (recordedSamples.isEmpty()) return null
            captured = FloatArray(recordedSamples.size) { recordedSamples[it] }
            recordedSamples.clear()
        }

        val info = AudioDecoder.analyzeBuffer(
            id = "mic_${System.currentTimeMillis()}",
            title = "MIC_CAPTURE_${captured.size / sampleRate}S.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = captured,
            right = captured.copyOf(),
            sampleRate = sampleRate,
            channels = 1,
            isBundled = false
        )
        val rightCopy = captured.copyOf()
        loadBuffers(captured, rightCopy, info.durationMs, info.integratedLufs, info.truePeakDb)
        return DecodedAudio(captured, rightCopy, sampleRate, info)
    }

    fun getCurrentBuffers(): Pair<FloatArray, FloatArray> = leftPcm to rightPcm

    fun release() {
        pause()
        isRecordingFlag = false
        recordJob?.cancel()
        recordJob = null
    }

    private fun computeIdleBaselineLatencyMs(): Float {
        var sum = 0f
        for (hook in hooks) {
            if (hook.isEnabled) {
                sum += hook.baselineLatencyMs
            }
        }
        return sum
    }

    private fun publishHookSnapshots() {
        val bypassed = targetWetGain <= 0.01f
        _hookSnapshots.value = hooks.map { hook ->
            val ms = when {
                bypassed || !hook.isEnabled -> 0f
                isPlayingFlag -> perHookEmaMs[hook.id] ?: hook.baselineLatencyMs
                else -> perHookEmaMs[hook.id]?.takeIf { it > 0f && smoothedChainLatencyMs > 0f }
                    ?: hook.baselineLatencyMs
            }
            DspHookSnapshot(hook.id, hook.name, hook.isEnabled, ms)
        }
    }

    private fun emitTelemetryBlock(
        monoAnalysis: FloatArray,
        scopePreview: FloatArray,
        sumSqL: Float,
        sumSqR: Float,
        peakBlock: Float,
        midEnergy: Float,
        sideEnergy: Float,
        frameCount: Int,
        positionMs: Long,
        durationMs: Long,
        isPlaying: Boolean
    ) {
        val bins64 = computeSpectrum64(monoAnalysis)
        val rmsL = sqrt((sumSqL / frameCount).coerceAtLeast(1e-12f))
        val rmsR = sqrt((sumSqR / frameCount).coerceAtLeast(1e-12f))
        val vuL = (20f * log10(rmsL.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val vuR = (20f * log10(rmsR.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val tpDb = (20f * log10(peakBlock.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val lufs = (((vuL + vuR) * 0.5f) - 0.6f).coerceIn(-48f, -2f)
        val corr = ((midEnergy - sideEnergy) / (midEnergy + sideEnergy).coerceAtLeast(1e-6f))
            .coerceIn(-1f, 1f)

        val glyph = listOf(
            bins64.slice(0..7).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(8..19).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(20..35).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(36..51).average().toFloat().coerceIn(0.05f, 1f),
            peakBlock.coerceIn(0.05f, 1f)
        )

        _telemetry.value = _telemetry.value.copy(
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs,
            spectrumBins = bins64,
            scopeSamples = scopePreview.toList(),
            leftVuDb = vuL,
            rightVuDb = vuR,
            integratedLufs = lufs,
            truePeakDb = tpDb,
            stereoCorrelation = corr,
            glyphZones = glyph
        )
    }

    private fun computeSpectrum64(samples: FloatArray): List<Float> {
        val n = 256
        val re = FloatArray(n)
        val im = FloatArray(n)
        for (i in 0 until n) {
            val hann = 0.5f * (1f - cos(2f * PI.toFloat() * i / (n - 1)))
            re[i] = samples[i] * hann
        }

        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
            }
        }

        var len = 2
        while (len <= n) {
            val ang = (-2.0 * PI / len).toFloat()
            val wLenRe = cos(ang)
            val wLenIm = sin(ang)
            for (i in 0 until n step len) {
                var wRe = 1f
                var wIm = 0f
                for (k in 0 until len / 2) {
                    val uRe = re[i + k]
                    val uIm = im[i + k]
                    val vRe = re[i + k + len / 2] * wRe - im[i + k + len / 2] * wIm
                    val vIm = re[i + k + len / 2] * wIm + im[i + k + len / 2] * wRe
                    re[i + k] = uRe + vRe
                    im[i + k] = uIm + vIm
                    re[i + k + len / 2] = uRe - vRe
                    im[i + k + len / 2] = uIm - vIm
                    val nextWRe = wRe * wLenRe - wIm * wLenIm
                    wIm = wRe * wLenIm + wIm * wLenRe
                    wRe = nextWRe
                }
            }
            len = len shl 1
        }

        val half = n / 2
        return List(64) { bar ->
            val binIdx = (1f * (half - 1).toFloat().pow(bar / 63f)).toInt().coerceIn(1, half - 1)
            val mag = sqrt(re[binIdx] * re[binIdx] + im[binIdx] * im[binIdx]) / (n * 0.25f)
            ((20f * log10(mag.coerceAtLeast(1e-4f)) + 52f) / 52f).coerceIn(0.04f, 1f)
        }
    }

    companion object {
        private const val LATENCY_EMA_ALPHA = 0.22f

        fun hasRecordAudioPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun parsePcmBytes(
            bytes: ByteArray,
            fallbackSampleRate: Int = 48000,
            fallbackChannels: Int = 2
        ): Pair<FloatArray, FloatArray> {
            val isRiff = bytes.size >= 44 &&
                bytes[0] == 'R'.code.toByte() &&
                bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() &&
                bytes[3] == 'F'.code.toByte()

            var dataOffset = 0
            var channels = fallbackChannels.coerceIn(1, 2)
            var bitDepth = 16
            var audioFormat = 1

            if (isRiff) {
                val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                audioFormat = bb.getShort(20).toInt() and 0xFFFF
                channels = bb.getShort(22).toInt().coerceIn(1, 2)
                bitDepth = bb.getShort(34).toInt().coerceAtLeast(16)
                dataOffset = 44
            }

            val payloadSize = (bytes.size - dataOffset).coerceAtLeast(0)
            val bytesPerSample = when (bitDepth) {
                32 -> 4
                24 -> 3
                else -> 2
            }
            val frameCount = (payloadSize / (channels * bytesPerSample)).coerceAtLeast(1)
            val left = FloatArray(frameCount)
            val right = FloatArray(frameCount)

            var ptr = dataOffset
            for (f in 0 until frameCount) {
                val l = readNormalizedSample(bytes, ptr, bytesPerSample, audioFormat)
                ptr += bytesPerSample
                val r = if (channels >= 2 && ptr + bytesPerSample <= bytes.size) {
                    val v = readNormalizedSample(bytes, ptr, bytesPerSample, audioFormat)
                    ptr += bytesPerSample
                    v
                } else {
                    l
                }
                left[f] = l
                right[f] = r
            }
            return left to right
        }

        private fun readNormalizedSample(
            bytes: ByteArray,
            offset: Int,
            bytesPerSample: Int,
            audioFormat: Int = 1
        ): Float {
            if (offset + bytesPerSample > bytes.size) return 0f
            return when (bytesPerSample) {
                4 -> {
                    val b0 = bytes[offset].toInt() and 0xFF
                    val b1 = bytes[offset + 1].toInt() and 0xFF
                    val b2 = bytes[offset + 2].toInt() and 0xFF
                    val b3 = bytes[offset + 3].toInt() and 0xFF
                    val bits = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
                    if (audioFormat == 3) {
                        java.lang.Float.intBitsToFloat(bits).let { if (it.isFinite()) it.coerceIn(-1f, 1f) else 0f }
                    } else {
                        (bits.toFloat() / 2147483648f).coerceIn(-1f, 1f)
                    }
                }
                3 -> {
                    val b0 = bytes[offset].toInt() and 0xFF
                    val b1 = bytes[offset + 1].toInt() and 0xFF
                    val b2 = bytes[offset + 2].toInt() // signed MSB
                    val raw = (b2 shl 16) or (b1 shl 8) or b0
                    (raw.toFloat() / 8388608f).coerceIn(-1f, 1f)
                }
                else -> {
                    val lo = bytes[offset].toInt() and 0xFF
                    val hi = bytes[offset + 1].toInt()
                    val s = ((hi shl 8) or lo).toShort()
                    (s.toFloat() / 32768f).coerceIn(-1f, 1f)
                }
            }
        }
    }
}
