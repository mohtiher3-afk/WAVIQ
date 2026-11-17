package com.example.audio

import android.content.Context
import com.example.model.AudioTelemetry
import com.example.model.EqBand
import com.example.model.ExportFormat
import com.example.model.GlyphHardwareFrame
import com.example.model.GlyphPatternMode
import com.example.model.OversamplingFactor
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import com.example.model.TranscodeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Coordinates WAVIQ's integrated Hi-Res, TFLite AI, Stem, and 4x/8x Oversampled DSP hooks
 * on top of [AudioProcessor].
 */
class AudioDspEngine(
    context: Context,
    scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    val audioProcessor = AudioProcessor(scope = scope, sampleRate = 48000, blockSize = 1024)

    private val eqHook = BiquadEqHook()
    private val tactileHook = TactileDriveHook()
    private val hiResHook = HiResUpsamplingHook()
    private val tfliteHook = TfliteDspHook(appContext, "ai_audio_mastering_model.tflite")
    private val stemHook = StemMatrixHook()
    private val limiterHook = TruePeakLimiterHook()
    private val glyphVuController: GlyphVuInterface = DefaultGlyphVuController()

    val sbrRestorationModule: Sbr192KhzRestorationModule
        get() = hiResHook

    val threeBandTruePeakLimiter: ThreeBandTruePeakLimiter
        get() = limiterHook

    val dsp64Pipeline: Dsp64BusEngine = Waviq64BitDspPipeline(
        stages = listOf(eqHook, tactileHook, hiResHook, tfliteHook, stemHook, limiterHook),
        sbrModule = hiResHook,
        truePeakLimiter = limiterHook
    )

    val bus64Config: Dsp64EngineConfig
        get() = Dsp64EngineConfig(
            sampleRate = audioProcessor.sampleRate,
            targetHiResKhz = hiResHook.targetKhz,
            oversamplingFactor = limiterHook.oversamplingFactor,
            ceilingDb = limiterHook.ceilingDb
        )

    @Volatile
    private var cachedEqBands: List<EqBand> = emptyList()
    @Volatile
    private var cachedDeckParams: TactileDeckParams = TactileDeckParams()
    @Volatile
    private var cachedStemMixerState: StemMixerState = StemMixerState()
    @Volatile
    private var cachedIsStemScreen: Boolean = false

    val telemetry: StateFlow<AudioTelemetry> = audioProcessor.telemetry
    val hookSnapshots: StateFlow<List<DspHookSnapshot>> = audioProcessor.hookSnapshots

    init {
        // Integrated 6-stage real-time 64-bit internal DSP chain
        audioProcessor.registerHook(eqHook)
        audioProcessor.registerHook(tactileHook)
        audioProcessor.registerHook(hiResHook)
        audioProcessor.registerHook(tfliteHook)
        audioProcessor.registerHook(stemHook)
        audioProcessor.registerHook(limiterHook)
    }

    fun loadDecodedAudio(audio: DecodedAudio) {
        glyphVuController.reset()
        audioProcessor.loadBuffers(
            left = audio.left,
            right = audio.right,
            infoDurationMs = audio.info.durationMs,
            lufs = audio.info.integratedLufs,
            peakDb = audio.info.truePeakDb
        )
    }

    fun loadLocalPcmFile(file: File): DecodedAudio = audioProcessor.loadLocalPcmFile(file)

    fun updateDsp(
        bands: List<EqBand>,
        deck: TactileDeckParams,
        stems: StemMixerState,
        isStemScreen: Boolean,
        wetEnabled: Boolean
    ) {
        cachedEqBands = bands
        cachedDeckParams = deck
        cachedStemMixerState = stems
        cachedIsStemScreen = isStemScreen

        eqHook.configure(bands, audioProcessor.sampleRate)
        tactileHook.configure(deck, audioProcessor.sampleRate)
        hiResHook.configure(audioProcessor.sampleRate)
        stemHook.updateState(stems, isStemScreen)
        limiterHook.ceilingDb = deck.ceilingDb
        audioProcessor.setWetEnabled(wetEnabled)
    }

    fun setOversamplingFactor(factor: OversamplingFactor) {
        limiterHook.oversamplingFactor = factor
        audioProcessor.refreshLatencyTelemetry()
    }

    fun setHiResTargetKhz(khz: Int) {
        hiResHook.targetKhz = khz
        audioProcessor.refreshLatencyTelemetry()
    }

    fun setHiResRestorationAmount(amount: Float) {
        hiResHook.restorationAmount = amount.coerceIn(0.25f, 1.0f)
    }

    fun getHiResRestorationAmount(): Float = hiResHook.restorationAmount

    fun setHiResStereoExpansionAmount(amount: Float) {
        hiResHook.stereoExpansionAmount = amount.coerceIn(0.0f, 1.0f)
    }

    fun enableAutonomousMasteringHooks() {
        audioProcessor.setHookEnabled(eqHook.id, true)
        audioProcessor.setHookEnabled(tactileHook.id, true)
        audioProcessor.setHookEnabled(hiResHook.id, true)
        audioProcessor.setHookEnabled(tfliteHook.id, true)
        audioProcessor.setHookEnabled(limiterHook.id, true)
    }

    fun getTfliteRuntimeLabel(): String = tfliteHook.runtimeMode

    fun getNeuralTelemetry(): Pair<Float, Float> {
        return if (tfliteHook.isEnabled) {
            tfliteHook.lastGateReductionPct to tfliteHook.lastDynGainDb
        } else {
            0f to 0f
        }
    }

    fun getLimiterGainReductionDb(): Float {
        return if (limiterHook.isEnabled) limiterHook.lastGainReductionDb else 0f
    }

    fun getMultiBandLimiterTelemetry(): Triple<Float, Float, Float> {
        return if (limiterHook.isEnabled) {
            Triple(
                limiterHook.lastLowBandGrDb,
                limiterHook.lastMidBandGrDb,
                limiterHook.lastHighBandGrDb
            )
        } else {
            Triple(0f, 0f, 0f)
        }
    }

    fun getSbrMetrics(): SbrRestorationMetrics = hiResHook.readSbrMetrics()

    fun getLimiterTelemetry(): ThreeBandLimiterTelemetry = limiterHook.readLimiterTelemetry()

    fun createOfflineDsp64Pipeline(
        internalSampleRate: Int,
        config: TranscodeConfig
    ): Pair<Dsp64BusEngine, TfliteDspHook> {
        val offEq = BiquadEqHook().apply {
            isEnabled = eqHook.isEnabled
            configure(cachedEqBands, internalSampleRate)
        }
        val offTactile = TactileDriveHook().apply {
            isEnabled = tactileHook.isEnabled
            configure(cachedDeckParams, internalSampleRate)
        }
        val offHiRes = HiResUpsamplingHook().apply {
            isEnabled = hiResHook.isEnabled
            configure(
                sampleRate = internalSampleRate,
                targetKhz = hiResHook.targetKhz,
                restorationAmount = hiResHook.restorationAmount,
                stereoExpansionAmount = hiResHook.stereoExpansionAmount
            )
        }
        val offTflite = TfliteDspHook(appContext, "ai_audio_mastering_model.tflite").apply {
            isEnabled = tfliteHook.isEnabled
        }
        val offBandMixer = StemMatrixHook().apply {
            isEnabled = stemHook.isEnabled
            updateState(cachedStemMixerState, cachedIsStemScreen)
            configureCrossovers(internalSampleRate)
        }
        val offLimiter = TruePeakLimiterHook().apply {
            isEnabled = limiterHook.isEnabled
            configure(
                ceilingDb = if (config.normalizeToTarget) config.targetTruePeakDb else limiterHook.ceilingDb,
                oversamplingFactor = if (config.format.sampleRate >= 192000) {
                    OversamplingFactor.X8
                } else {
                    limiterHook.oversamplingFactor
                },
                sampleRate = internalSampleRate
            )
        }
        val pipeline = Waviq64BitDspPipeline(
            stages = listOf(offEq, offTactile, offHiRes, offTflite, offBandMixer, offLimiter),
            sbrModule = offHiRes,
            truePeakLimiter = offLimiter
        )
        return pipeline to offTflite
    }

    fun buildGlyphFrame(
        tel: AudioTelemetry,
        neuralGatePct: Float,
        limiterGrDb: Float,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame {
        val peakLinear = 10.0.pow((tel.truePeakDb / 20.0).toDouble()).toFloat().coerceIn(0f, 1f)
        return glyphVuController.mapToHardwareFrame(
            rmsLeftDb = tel.leftVuDb,
            rmsRightDb = tel.rightVuDb,
            limiterGainReductionDb = limiterGrDb,
            neuralGateReductionPct = neuralGatePct,
            peakBlock = peakLinear,
            bins64 = tel.spectrumBins,
            patternMode = patternMode
        )
    }

    fun toggleHook(hookId: String, enabled: Boolean) {
        audioProcessor.setHookEnabled(hookId, enabled)
    }

    fun togglePlayPause() = audioProcessor.togglePlayPause()

    fun play() = audioProcessor.play()

    fun pause() = audioProcessor.pause()

    fun seekToFraction(fraction: Float) = audioProcessor.seekToFraction(fraction)

    fun startMicRecording(
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) = audioProcessor.startRecording(onMeterUpdate, onError)

    fun stopMicRecording(): DecodedAudio? = audioProcessor.stopRecording()

    fun release() {
        audioProcessor.release()
        tfliteHook.close()
    }

    suspend fun exportMasterWav(
        outFile: File,
        config: TranscodeConfig,
        onProgress: (Float, Long) -> Unit
    ): Pair<Float, Float> = withContext(Dispatchers.IO) {
        val (srcL, srcR) = audioProcessor.getCurrentBuffers()
        val total = srcL.size.coerceAtLeast(1)
        val internalSr = audioProcessor.sampleRate

        val outL = srcL.copyOf()
        val outR = srcR.copyOf()

        // Run offline chunked pass natively inside pure 64-bit DoubleArray buffers
        val (offlinePipeline, offTflite) = createOfflineDsp64Pipeline(internalSr, config)

        val chunkSize = 1024
        val blk64L = DoubleArray(chunkSize)
        val blk64R = DoubleArray(chunkSize)
        var pos = 0

        val isWet = audioProcessor.isWetEnabled()

        offTflite.use {
            while (pos < total) {
                val count = minOf(chunkSize, total - pos)
                for (i in 0 until count) {
                    blk64L[i] = outL[pos + i].toDouble()
                    blk64R[i] = outR[pos + i].toDouble()
                }

                if (isWet) {
                    // Exact match to live 64-bit chain order:
                    // Eq -> TactileDrive -> 192kHz SBR -> Tflite -> BandMixer -> 3-Band TruePeakLimiter
                    offlinePipeline.processChainDouble(blk64L, blk64R, count, internalSr)
                }

                for (i in 0 until count) {
                    outL[pos + i] = blk64L[i].coerceIn(-1.0, 1.0).toFloat()
                    outR[pos + i] = blk64R[i].coerceIn(-1.0, 1.0).toFloat()
                }

                pos += count
                if (pos % (chunkSize * 40) == 0) {
                    onProgress((pos.toFloat() / total) * 0.45f, 0L)
                }
            }
        }

        val targetSr = config.format.sampleRate
        val (finL, finR) = if (targetSr == internalSr) {
            outL to outR
        } else {
            AudioMeasurement.resampleWithAntiAliasing(outL, outR, internalSr, targetSr)
        }

        // Normalize ceiling cleanly against 4x oversampled True-Peak dBTP target when requested (in wet mode)
        if (isWet && config.normalizeToTarget) {
            val postTruePeak = AudioMeasurement.measureTruePeakLinear(finL, finR).coerceAtLeast(1e-7)
            val targetCeilingAmp = 10.0.pow(config.targetTruePeakDb.toDouble() / 20.0).coerceIn(0.25, 0.995)
            val normScale = (targetCeilingAmp / postTruePeak).coerceIn(0.5, 4.0)
            if (abs(normScale - 1.0) > 0.002) {
                for (i in finL.indices) {
                    finL[i] = (finL[i].toDouble() * normScale).coerceIn(-targetCeilingAmp, targetCeilingAmp).toFloat()
                    finR[i] = (finR[i].toDouble() * normScale).coerceIn(-targetCeilingAmp, targetCeilingAmp).toFloat()
                }
            }
        }

        val bitDepth = config.format.bitDepth
        val isFloat32 = bitDepth == 32
        val audioFormatTag: Short = if (isFloat32) 3 else 1 // 3 = IEEE_FLOAT, 1 = PCM
        val bytesPerSample = bitDepth / 8
        val dataSize = finL.size * 2 * bytesPerSample

        val applyDither = config.ditherEnabled && !isFloat32
        val rng = java.util.Random(0x5EED2026L)
        val tpdfLsbStep = when (bitDepth) {
            16 -> 1.0 / 32767.0
            24 -> 1.0 / 8388607.0
            else -> 0.0
        }

        outFile.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(outFile)).use { bos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + dataSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(audioFormatTag)
            header.putShort(2)
            header.putInt(targetSr)
            header.putInt(targetSr * 2 * bytesPerSample)
            header.putShort((2 * bytesPerSample).toShort())
            header.putShort(bitDepth.toShort())
            header.put("data".toByteArray())
            header.putInt(dataSize)
            bos.write(header.array())

            val chunkFrames = 4096
            val byteBuf = ByteArray(chunkFrames * 2 * bytesPerSample)
            var written = 44L
            var offset = 0
            while (offset < finL.size) {
                val count = minOf(chunkFrames, finL.size - offset)
                var ptr = 0
                for (k in 0 until count) {
                    val baseL = finL[offset + k].toDouble().coerceIn(-1.0, 1.0)
                    val baseR = finR[offset + k].toDouble().coerceIn(-1.0, 1.0)

                    if (isFloat32) {
                        val bitsL = java.lang.Float.floatToRawIntBits(baseL.toFloat())
                        val bitsR = java.lang.Float.floatToRawIntBits(baseR.toFloat())
                        byteBuf[ptr++] = (bitsL and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 8) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 16) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 24) and 0xFF).toByte()
                        byteBuf[ptr++] = (bitsR and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 8) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 16) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 24) and 0xFF).toByte()
                    } else {
                        val ditherL = if (applyDither) (rng.nextDouble() - rng.nextDouble()) * tpdfLsbStep else 0.0
                        val ditherR = if (applyDither) (rng.nextDouble() - rng.nextDouble()) * tpdfLsbStep else 0.0
                        val dL = (baseL + ditherL).coerceIn(-1.0, 1.0)
                        val dR = (baseR + ditherR).coerceIn(-1.0, 1.0)

                        if (bitDepth == 16) {
                            val sL = kotlin.math.round(dL * 32767.0).toInt().coerceIn(-32768, 32767)
                            val sR = kotlin.math.round(dR * 32767.0).toInt().coerceIn(-32768, 32767)
                            byteBuf[ptr++] = (sL and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = (sR and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 8) and 0xFF).toByte()
                        } else {
                            val sL = kotlin.math.round(dL * 8388607.0).toInt().coerceIn(-8388608, 8388607)
                            val sR = kotlin.math.round(dR * 8388607.0).toInt().coerceIn(-8388608, 8388607)
                            byteBuf[ptr++] = (sL and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 16) and 0xFF).toByte()
                            byteBuf[ptr++] = (sR and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 16) and 0xFF).toByte()
                        }
                    }
                }
                bos.write(byteBuf, 0, ptr)
                written += ptr
                offset += count
                onProgress(0.45f + (offset.toFloat() / finL.size) * 0.55f, written)
            }
            bos.flush()
        }

        val finalLufs = AudioMeasurement.measureIntegratedLufs(finL, finR, targetSr)
        val finalTruePeakDb = AudioMeasurement.measureTruePeakDb(finL, finR)
        finalLufs to finalTruePeakDb
    }
}
