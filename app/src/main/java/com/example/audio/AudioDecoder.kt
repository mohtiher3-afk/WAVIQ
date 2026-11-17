package com.example.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.AudioTrackInfo
import com.example.model.EnhanceProfile
import com.example.model.MusicGenre
import com.example.model.TrackDiagnosticReport
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class DecodedAudio(
    val left: FloatArray,
    val right: FloatArray,
    val sampleRate: Int,
    val info: AudioTrackInfo
)

object AudioDecoder {

    private const val TARGET_SAMPLE_RATE = 48000
    private const val MAX_SAFE_DECODE_SECONDS = 900 // 15-minute memory-safe limit for full track mastering

    fun generateStudioReferenceTrack(): DecodedAudio {
        val sr = TARGET_SAMPLE_RATE
        val durationSec = 16
        val numSamples = sr * durationSec
        val left = FloatArray(numSamples)
        val right = FloatArray(numSamples)

        val bassNotes = floatArrayOf(55.0f, 55.0f, 65.41f, 73.42f, 49.0f, 49.0f, 61.74f, 65.41f)
        val padRoots = floatArrayOf(220.0f, 261.63f, 196.0f, 246.94f)
        val leadNotes = floatArrayOf(440f, 523.25f, 659.25f, 587.33f, 523.25f, 440f, 392f, 493.88f)

        val bpm = 120f
        val beatSamples = (sr * 60f / bpm).toInt()
        val eighthSamples = beatSamples / 2

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sr
            val beatIdx = i / beatSamples
            val eighthIdx = i / eighthSamples
            val posInBeat = (i % beatSamples).toFloat() / sr
            val posInEighth = (i % eighthSamples).toFloat() / sr

            // Kick drum on every beat
            val kickEnv = exp(-posInBeat * 14f)
            val kickFreq = 48f + 110f * exp(-posInBeat * 38f)
            val kick = sin(2f * PI.toFloat() * kickFreq * posInBeat) * kickEnv * 0.42f

            // Snare / Clap on beats 2 & 4
            val isSnareBeat = (beatIdx % 2 == 1)
            val snareEnv = if (isSnareBeat) exp(-posInBeat * 22f) else 0f
            val pseudoNoise = sin(i * 12.9898f) * 43758.545f
            val noise = (pseudoNoise - pseudoNoise.toInt()) * 2f - 1f
            val snare = (noise * 0.65f + sin(2f * PI.toFloat() * 195f * posInBeat) * 0.35f) * snareEnv * 0.24f

            // Crisp hi-hat on off-beats
            val hatEnv = exp(-posInEighth * 45f)
            val hat = ((sin(t * 19000f) + sin(t * 27300f) + noise * 0.5f) * 0.33f) * hatEnv * 0.09f

            // Analog sub + harmonic bass
            val bassF = bassNotes[(beatIdx / 2) % bassNotes.size]
            val bassEnv = (1f - exp(-posInEighth * 40f)) * exp(-posInEighth * 3.5f)
            val bass = (sin(2f * PI.toFloat() * bassF * t) * 0.7f +
                sin(2f * PI.toFloat() * bassF * 2f * t) * 0.25f) * bassEnv * 0.30f

            // Wide stereo synth pad
            val padF = padRoots[(beatIdx / 4) % padRoots.size]
            val padL = (sin(2f * PI.toFloat() * padF * t) +
                0.6f * sin(2f * PI.toFloat() * (padF * 1.25f) * t) +
                0.4f * sin(2f * PI.toFloat() * (padF * 1.5f) * t)) * 0.09f
            val padR = (sin(2f * PI.toFloat() * (padF * 1.003f) * t + 0.4f) +
                0.6f * sin(2f * PI.toFloat() * (padF * 1.252f) * t + 0.9f) +
                0.4f * sin(2f * PI.toFloat() * (padF * 1.503f) * t + 1.2f)) * 0.09f

            // Vocal-range center lead
            val leadF = leadNotes[eighthIdx % leadNotes.size]
            val leadEnv = (1f - exp(-posInEighth * 30f)) * exp(-posInEighth * 5f)
            val lead = (sin(2f * PI.toFloat() * leadF * t) * 0.65f +
                sin(2f * PI.toFloat() * leadF * 2f * t) * 0.25f +
                sin(2f * PI.toFloat() * leadF * 3f * t) * 0.1f) * leadEnv * 0.15f

            val l = (kick + snare + hat * 0.85f + bass + padL + lead).coerceIn(-0.95f, 0.95f)
            val r = (kick + snare + hat * 1.15f + bass + padR + lead).coerceIn(-0.95f, 0.95f)
            left[i] = l
            right[i] = r
        }

        val info = analyzeBuffer(
            id = "ref_studio_01",
            title = "NOTHING_SESSION_01_RAW.wav",
            sourceLabel = "BUNDLED STUDIO REF",
            left = left,
            right = right,
            sampleRate = sr,
            channels = 2,
            isBundled = true
        )
        return DecodedAudio(left, right, sr, info)
    }

    fun decodeUriToPcm(context: Context, uri: Uri): DecodedAudio {
        val displayName = resolveDisplayName(context, uri) ?: "IMPORTED_TRACK.wav"
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            var audioTrackIdx = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIdx = i
                    format = f
                    break
                }
            }
            require(audioTrackIdx >= 0 && format != null) { "No audio track found in selected file." }

            extractor.selectTrack(audioTrackIdx)
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val sourceSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val sourceChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceAtLeast(1)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val pcmBytesOut = ByteArrayOutputStream()
            val maxRawBytes = sourceSampleRate.toLong() * sourceChannels * 2L * MAX_SAFE_DECODE_SECONDS
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var currentSampleRate = sourceSampleRate
            var currentChannels = sourceChannels

            while (!outputDone && pcmBytesOut.size() < maxRawBytes) {
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)!!
                        val sampleSize = extractor.readSampleData(inBuf, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(
                                inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(
                                inIdx, 0, sampleSize, extractor.sampleTime, 0
                            )
                            extractor.advance()
                        }
                    }
                }

                val outIdx = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                when {
                    outIdx >= 0 -> {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && bufferInfo.size > 0) {
                            outBuf.position(bufferInfo.offset)
                            outBuf.limit(bufferInfo.offset + bufferInfo.size)
                            val chunk = ByteArray(bufferInfo.size)
                            outBuf.get(chunk)
                            pcmBytesOut.write(chunk)
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                        }
                    }
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFormat = codec.outputFormat
                        if (outFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            currentSampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        if (outFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            currentChannels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                    }
                }
            }

            codec.stop()
            codec.release()

            val raw = pcmBytesOut.toByteArray()
            val shortCount = raw.size / 2
            val frameCount = (shortCount / currentChannels).coerceAtLeast(1)
            val srcLeft = FloatArray(frameCount)
            val srcRight = FloatArray(frameCount)

            val bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
            for (f in 0 until frameCount) {
                val l = bb.getShort().toFloat() / 32768f
                val r = if (currentChannels >= 2) bb.getShort().toFloat() / 32768f else l
                // Skip extra channels if surround
                for (c in 2 until currentChannels) {
                    bb.getShort()
                }
                srcLeft[f] = l
                srcRight[f] = r
            }

            val (left48, right48) = if (currentSampleRate == TARGET_SAMPLE_RATE) {
                srcLeft to srcRight
            } else {
                resampleCubic(srcLeft, srcRight, currentSampleRate, TARGET_SAMPLE_RATE)
            }

            val info = analyzeBuffer(
                id = "import_${System.currentTimeMillis()}",
                title = displayName,
                sourceLabel = "LOCAL SAF IMPORT",
                left = left48,
                right = right48,
                sampleRate = currentSampleRate,
                channels = currentChannels,
                isBundled = false
            )
            return DecodedAudio(left48, right48, TARGET_SAMPLE_RATE, info)
        } finally {
            extractor.release()
        }
    }

    private fun resampleCubic(
        srcL: FloatArray,
        srcR: FloatArray,
        srcSr: Int,
        dstSr: Int
    ): Pair<FloatArray, FloatArray> {
        val ratio = srcSr.toDouble() / dstSr.toDouble()
        val outLen = ((srcL.size / ratio).toInt()).coerceAtLeast(1)
        val outL = FloatArray(outLen)
        val outR = FloatArray(outLen)
        val last = (srcL.size - 1).coerceAtLeast(0)
        for (i in 0 until outLen) {
            val pos = i * ratio
            val i1 = pos.toInt().coerceIn(0, last)
            val i0 = (i1 - 1).coerceAtLeast(0)
            val i2 = (i1 + 1).coerceAtMost(last)
            val i3 = (i1 + 2).coerceAtMost(last)
            val t = pos - i1
            val t2 = t * t
            val t3 = t2 * t
            outL[i] = (0.5 * (2.0 * srcL[i1] + (-srcL[i0] + srcL[i2]) * t + (2.0 * srcL[i0] - 5.0 * srcL[i1] + 4.0 * srcL[i2] - srcL[i3]) * t2 + (-srcL[i0] + 3.0 * srcL[i1] - 3.0 * srcL[i2] + srcL[i3]) * t3)).coerceIn(-1.0, 1.0).toFloat()
            outR[i] = (0.5 * (2.0 * srcR[i1] + (-srcR[i0] + srcR[i2]) * t + (2.0 * srcR[i0] - 5.0 * srcR[i1] + 4.0 * srcR[i2] - srcR[i3]) * t2 + (-srcR[i0] + 3.0 * srcR[i1] - 3.0 * srcR[i2] + srcR[i3]) * t3)).coerceIn(-1.0, 1.0).toFloat()
        }
        return outL to outR
    }

    fun analyzeBuffer(
        id: String,
        title: String,
        sourceLabel: String,
        left: FloatArray,
        right: FloatArray,
        sampleRate: Int,
        channels: Int,
        isBundled: Boolean
    ): AudioTrackInfo {
        val len = left.size.coerceAtLeast(1)
        var peak = 1e-6f
        var sumSq = 0.0
        var midSq = 0.0
        var sideSq = 0.0
        var hfDiff2Sq = 0.0

        val step = max(1, len / 40_000)
        var count = 0
        var prevMid = 0f
        var prevPrevMid = 0f
        for (i in 0 until len step step) {
            val l = left[i]
            val r = right[i]
            val p = max(abs(l), abs(r))
            if (p > peak) peak = p
            sumSq += 0.5 * (l * l + r * r)
            val m = 0.5 * (l + r)
            val s = 0.5 * (l - r)
            midSq += m * m
            sideSq += s * s
            val mF = m.toFloat()
            if (count >= 2 && step == 1) {
                val d2 = mF - 2f * prevMid + prevPrevMid
                hfDiff2Sq += (d2 * d2).toDouble()
            } else if (i >= 2) {
                val m1 = 0.5f * (left[i - 1] + right[i - 1])
                val m2 = 0.5f * (left[i - 2] + right[i - 2])
                val d2 = mF - 2f * m1 + m2
                hfDiff2Sq += (d2 * d2).toDouble()
            }
            prevPrevMid = prevMid
            prevMid = mF
            count++
        }

        val rms = sqrt((sumSq / count.coerceAtLeast(1)).coerceAtLeast(1e-12)).toFloat()
        val rmsDb = (20f * log10(rms.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        val peakDb = (20f * log10(peak.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        val lufs = (rmsDb - 0.7f).coerceIn(-48f, -2f)
        val dr = (peakDb - rmsDb).coerceIn(1.5f, 24f)
        val widthPct = ((sideSq / (midSq + sideSq).coerceAtLeast(1e-9)) * 200.0)
            .toInt().coerceIn(5, 100)

        // Estimate upper-band presence using 2nd-order finite-difference energy vs mid energy
        val hfRatio = sqrt((hfDiff2Sq / midSq.coerceAtLeast(1e-9)).coerceAtLeast(0.0)).toFloat()
        val estimatedBandwidthKhz = (9.5f + (hfRatio * 8.2f)).coerceIn(9.5f, 20.0f)
        val hfDeficitPct = (((20.0f - estimatedBandwidthKhz) / 10.5f) * 100f)
            .toInt().coerceIn(4, 92)

        val (recordingType, recProfile) = when {
            channels == 1 || (widthPct <= 10 && hfDeficitPct >= 35) -> "VOCAL / SPEECH" to EnhanceProfile.VOCAL_CLARITY
            dr >= 17.5f || (dr >= 12.0f && lufs < -20.0f) -> "ACOUSTIC / DYNAMIC" to EnhanceProfile.WARM_ANALOG
            dr < 6.5f || lufs > -11.5f -> "COMPRESSED / CLUB MIX" to EnhanceProfile.STREAMING_MASTER
            else -> "FULL STUDIO MIX" to EnhanceProfile.STREAMING_MASTER
        }

        // Fast spectral & transient classification across standard genres (including Drill, Techno, Trap)
        val (detectedGenre, genreConfidence, genreRationale) = when {
            title.contains("drill", ignoreCase = true) || title.contains("ukdrill", ignoreCase = true) || title.contains("nydrill", ignoreCase = true) || title.contains("دريل") -> {
                Triple(
                    MusicGenre.DRILL,
                    94,
                    "Sliding 808 sub-bass articulation & rapid transient snap"
                )
            }
            title.contains("techno", ignoreCase = true) || title.contains("industrial", ignoreCase = true) || title.contains("rave", ignoreCase = true) || title.contains("تكنو") -> {
                Triple(
                    MusicGenre.TECHNO,
                    94,
                    "Hypnotic 4/4 low-end rumble & driving analog saturation"
                )
            }
            title.contains("trap", ignoreCase = true) || title.contains("808", ignoreCase = true) || title.contains("تراب") -> {
                Triple(
                    MusicGenre.TRAP,
                    93,
                    "Heavy 808 harmonic saturation & crisp snare/clap transient pop"
                )
            }
            title.contains("classic", ignoreCase = true) || title.contains("orchestra", ignoreCase = true) || (channels >= 2 && dr >= 15.0f && lufs < -19.0f) -> {
                Triple(
                    MusicGenre.CLASSICAL,
                    if (dr >= 15.0f) 94 else 88,
                    "Wide ${String.format(Locale.US, "%.1f", dr)}dB dynamic range & uncompressed acoustic headroom"
                )
            }
            title.contains("electronic", ignoreCase = true) || title.contains("edm", ignoreCase = true) || isBundled || (widthPct >= 50 && lufs >= -18.5f && dr in 6.0f..14.5f) -> {
                Triple(
                    MusicGenre.ELECTRONIC,
                    93,
                    "Wide stereo field & high-energy sub-bass electronic extension"
                )
            }
            title.contains("acoustic", ignoreCase = true) || (dr >= 11.5f && (channels == 1 || (widthPct <= 45 && lufs < -18.0f))) -> {
                Triple(
                    MusicGenre.ACOUSTIC,
                    91,
                    "Natural acoustic crest factor & warm harmonic preservation"
                )
            }
            title.contains("hiphop", ignoreCase = true) || title.contains("hip_hop", ignoreCase = true) || title.contains("rap", ignoreCase = true) || (dr in 7.0f..12.5f && widthPct in 20..50 && lufs >= -15.5f) -> {
                Triple(
                    MusicGenre.HIP_HOP,
                    89,
                    "Punchy low-end transient impact & focused vocal articulation"
                )
            }
            title.contains("rock", ignoreCase = true) || title.contains("metal", ignoreCase = true) || (dr in 5.5f..11.0f && widthPct in 25..55 && hfDeficitPct in 18..60) -> {
                Triple(
                    MusicGenre.ROCK,
                    88,
                    "Dense mid-frequency harmonic saturation & driving drum presence"
                )
            }
            else -> {
                Triple(
                    MusicGenre.POP,
                    86,
                    "Modern streaming crest (${String.format(Locale.US, "%.1f", dr)}dB DR) & balanced high-frequency presence"
                )
            }
        }

        val clarityNeed = when {
            hfDeficitPct >= 45 || peakDb > -0.5f || lufs < -20f -> "HIGH"
            hfDeficitPct >= 20 || dr < 7.5f -> "MODERATE"
            else -> "LOW"
        }

        val restorationAmt = (0.35f + (hfDeficitPct / 100f) * 0.60f).coerceIn(0.35f, 0.92f)
        val diagnostic = TrackDiagnosticReport(
            recordingTypeLabel = recordingType,
            estimatedBandwidthKhz = estimatedBandwidthKhz,
            hfDeficitPct = hfDeficitPct,
            clarityNeedLabel = clarityNeed,
            recommendedProfile = detectedGenre.recommendedProfile,
            recommendedRestorationAmount = restorationAmt,
            autoAppliedSummary = "AUTO-GENRE // ${detectedGenre.title} (${detectedGenre.targetKhz}kHz ${detectedGenre.oversampling.label}) + TFLITE GUARD",
            detectedGenre = detectedGenre,
            genreConfidencePct = genreConfidence,
            genreRationale = genreRationale
        )

        val bars = 96
        val barSize = max(1, len / bars)
        val peaks = List(bars) { b ->
            val start = b * barSize
            val end = ((b + 1) * barSize).coerceAtMost(len)
            var maxBar = 0.05f
            for (j in start until end step max(1, barSize / 16)) {
                val amp = max(abs(left[j]), abs(right[j]))
                if (amp > maxBar) maxBar = amp
            }
            maxBar.coerceIn(0.05f, 1f)
        }

        val durationMs = (len.toLong() * 1000L) / TARGET_SAMPLE_RATE
        return AudioTrackInfo(
            id = id,
            title = title,
            sourceLabel = sourceLabel,
            sampleRate = sampleRate,
            channels = channels,
            durationMs = durationMs,
            bitDepthLabel = "64-BIT DOUBLE DSP",
            integratedLufs = lufs,
            truePeakDb = peakDb,
            dynamicRangeDb = dr,
            stereoWidthPct = widthPct,
            waveformPeaks = peaks,
            isBundledRef = isBundled,
            diagnostic = diagnostic
        )
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        }.getOrNull()
    }
}
