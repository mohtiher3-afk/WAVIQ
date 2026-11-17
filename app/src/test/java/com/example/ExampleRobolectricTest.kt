package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDecoder
import com.example.audio.AudioProcessor
import com.example.audio.BiquadEqHook
import com.example.audio.BiquadFilter
import com.example.audio.DefaultGlyphVuController
import com.example.audio.FilterType
import com.example.audio.GainTrimHook
import com.example.audio.GlyphPulseType
import com.example.audio.GlyphVUMapper
import com.example.audio.HiResUpsamplingHook
import com.example.audio.TfliteDspHook
import com.example.audio.TruePeakLimiterHook
import com.example.model.GlyphPatternMode
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches WAVIQ`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WAVIQ", appName)
    }

    @Test
    fun `route registry contains 16 unique screens mapped across 2 workspace tabs`() {
        val routes = WaviqRouteRegistry.allRoutes
        assertEquals(16, routes.size)
        assertEquals(16, routes.map { it.path }.distinct().size)
        assertEquals(2, WaviqTab.entries.size)
        assertEquals(12, WaviqRouteRegistry.byTab(WaviqTab.STUDIO).size)
        assertEquals(4, WaviqRouteRegistry.byTab(WaviqTab.LIBRARY).size)
        routes.forEach { spec ->
            assertTrue("Missing TS schema for ${spec.code}", spec.tsSchema.contains("export interface"))
        }
    }

    @Test
    fun `studio reference audio and biquad filter produce finite DSP output`() {
        val ref = AudioDecoder.generateStudioReferenceTrack()
        assertTrue(ref.left.isNotEmpty())
        assertEquals(48000, ref.sampleRate)
        assertEquals(96, ref.info.waveformPeaks.size)

        val filter = BiquadFilter(48000f)
        filter.configure(FilterType.PEAKING, 1000f, 1.0f, 6.0f)
        val out = filter.processLeft(0.5f)
        assertTrue(out.isFinite())
    }

    @Test
    fun `AudioProcessor registers toggles and executes Float32 DspHooks in order`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val processor = AudioProcessor(scope = scope)

        val hookA = GainTrimHook(id = "trim_a", name = "Trim A", gainLinear = 0.5f)
        val hookB = GainTrimHook(id = "trim_b", name = "Trim B", gainLinear = 0.5f)
        processor.registerHook(hookA)
        processor.registerHook(hookB)

        assertEquals(2, processor.hookSnapshots.value.size)

        val left = floatArrayOf(0.8f, -0.4f, 0.2f, 0.0f)
        val right = floatArrayOf(0.8f, -0.4f, 0.2f, 0.0f)
        processor.processThroughHooks(left, right, 4)
        assertEquals(0.2f, left[0], 0.001f)
        assertEquals(-0.1f, right[1], 0.001f)

        processor.setHookEnabled("trim_b", false)
        assertFalse(processor.hookSnapshots.value.first { it.id == "trim_b" }.isEnabled)
        val left2 = floatArrayOf(0.8f, 0.8f)
        val right2 = floatArrayOf(0.8f, 0.8f)
        processor.processThroughHooks(left2, right2, 2)
        assertEquals(0.4f, left2[0], 0.001f)

        processor.release()
        scope.cancel()
    }

    @Test
    fun `AudioProcessor parses 16-bit stereo PCM bytes into normalized Float32 buffers`() {
        val raw = byteArrayOf(
            0x00, 0x40,
            0x00, 0xC0.toByte()
        )
        val (l, r) = AudioProcessor.parsePcmBytes(raw, fallbackSampleRate = 48000, fallbackChannels = 2)
        assertEquals(1, l.size)
        assertEquals(0.5f, l[0], 0.001f)
        assertEquals(-0.5f, r[0], 0.001f)
    }

    @Test
    fun `bundled TFLite model asset exists and HiRes + Tflite + 4x8x TruePeak hooks process cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val modelBytes = context.assets.open("ai_audio_mastering_model.tflite").use { it.readBytes() }
        assertTrue(modelBytes.size > 100)
        assertEquals('T'.code.toByte(), modelBytes[4])
        assertEquals('F'.code.toByte(), modelBytes[5])
        assertEquals('L'.code.toByte(), modelBytes[6])
        assertEquals('3'.code.toByte(), modelBytes[7])

        val hiRes = HiResUpsamplingHook()
        val tflite = TfliteDspHook(context, "ai_audio_mastering_model.tflite")
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X4)

        val left = FloatArray(256) { i -> if (i % 4 == 0) 1.4f else -0.9f }
        val right = FloatArray(256) { i -> if (i % 4 == 0) 1.4f else -0.9f }

        hiRes.processBlock(left, right, 256, 48000)
        tflite.processBlock(left, right, 256, 48000)
        limiter.processBlock(left, right, 256, 48000)

        assertTrue(left.all { it.isFinite() && abs(it) <= 0.95f })

        // Verify 8x oversampling switch works cleanly
        limiter.oversamplingFactor = OversamplingFactor.X8
        val hotL = FloatArray(64) { 1.8f }
        val hotR = FloatArray(64) { -1.8f }
        limiter.processBlock(hotL, hotR, 64, 48000)
        assertTrue(hotL.all { it.isFinite() && abs(it) <= 0.95f })

        // Verify quiet hiss floor triggers measurable neural noise-gate attenuation and finite gain dB
        val noiseL = FloatArray(128) { i -> if (i % 2 == 0) 0.002f else -0.002f }
        val noiseR = FloatArray(128) { i -> if (i % 2 == 0) 0.002f else -0.002f }
        tflite.processBlock(noiseL, noiseR, 128, 48000)
        assertTrue(tflite.lastGateReductionPct > 0f && tflite.lastGateReductionPct <= 65f)
        assertTrue(tflite.lastDynGainDb.isFinite() && tflite.lastDynGainDb in -6f..6f)

        tflite.reset()
        assertEquals(0f, tflite.lastGateReductionPct, 0.0001f)
        assertEquals(0f, tflite.lastDynGainDb, 0.0001f)

        tflite.close()
    }

    @Test
    fun `smart track analysis classifies mono narrow speech vs wide stereo studio mix`() {
        val monoSpeech = FloatArray(4800) { i ->
            kotlin.math.sin(2.0 * Math.PI * 220.0 * i / 48000.0).toFloat() * 0.25f
        }
        val speechInfo = AudioDecoder.analyzeBuffer(
            id = "speech_test",
            title = "VOICE_NOTE.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = monoSpeech,
            right = monoSpeech.copyOf(),
            sampleRate = 48000,
            channels = 1,
            isBundled = false
        )
        assertEquals("VOCAL / SPEECH", speechInfo.diagnostic.recordingTypeLabel)
        assertTrue(speechInfo.diagnostic.hfDeficitPct > 20)
        assertTrue(speechInfo.diagnostic.recommendedRestorationAmount in 0.35f..0.92f)

        val studioRef = AudioDecoder.generateStudioReferenceTrack()
        assertEquals("FULL STUDIO MIX", studioRef.info.diagnostic.recordingTypeLabel)
    }

    @Test
    fun `GlyphVuInterface maps real-time DSP gain reduction and RMS levels to Nothing LED PWM patterns`() {
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X4)
        val hotL = FloatArray(128) { 1.6f }
        val hotR = FloatArray(128) { -1.6f }
        limiter.processBlock(hotL, hotR, 128, 48000)
        assertTrue(limiter.lastGainReductionDb > 1.0f)

        val mapper = DefaultGlyphVuController()
        val bins = List(64) { 0.45f }

        val stereoFrame = mapper.mapToHardwareFrame(
            rmsLeftDb = -10f,
            rmsRightDb = -18f,
            limiterGainReductionDb = limiter.lastGainReductionDb,
            neuralGateReductionPct = 22f,
            peakBlock = 0.89f,
            bins64 = bins,
            patternMode = GlyphPatternMode.STEREO_VU_BRIDGE
        )
        assertEquals(8, stereoFrame.centerRingSegments.size)
        assertEquals(6, stereoFrame.usbExclamationSegments.size)
        assertEquals(5, stereoFrame.pwm12BitChannels.size)
        assertTrue(stereoFrame.cameraPillLevel > stereoFrame.diagonalStripLevel)
        assertTrue(stereoFrame.pwm12BitChannels.all { it in 0..4095 })

        val grFrame = mapper.mapToHardwareFrame(
            rmsLeftDb = -14f,
            rmsRightDb = -14f,
            limiterGainReductionDb = 6.5f,
            neuralGateReductionPct = 30f,
            peakBlock = 0.92f,
            bins64 = bins,
            patternMode = GlyphPatternMode.GAIN_REDUCTION_CLAMP
        )
        assertEquals(GlyphPatternMode.GAIN_REDUCTION_CLAMP, grFrame.patternMode)
        assertTrue(grFrame.centerRingSegments[0] > 0.7f)
        assertTrue(grFrame.usbExclamationSegments.last() > 0.1f)
    }

    @Test
    fun `GlyphVUMapper normalizes RMS and Gain Reduction to 0-100 with fast attack and exponential decay`() {
        // Static companion normalization bounds & non-finite safety
        assertEquals(0, GlyphVUMapper.normalizeRmsDb(-60f))
        assertEquals(0, GlyphVUMapper.normalizeRmsDb(Float.NaN))
        assertEquals(100, GlyphVUMapper.normalizeRmsDb(-1f))
        assertEquals(0, GlyphVUMapper.normalizeRmsLinear(0f))
        assertEquals(0, GlyphVUMapper.normalizeGainReductionDb(-2f))
        assertEquals(50, GlyphVUMapper.normalizeGainReductionDb(3.0f))
        assertEquals(100, GlyphVUMapper.normalizeGainReductionDb(9.0f))

        val mapper = GlyphVUMapper()

        // 1. Idle baseline
        val idlePacket = mapper.mapToGlyphPulsePattern(-55f, -55f, 0f)
        assertEquals(GlyphPulseType.IDLE_HEARTBEAT, idlePacket.pulseType)
        assertEquals(0, idlePacket.rmsPercent)
        assertEquals(0, idlePacket.gainReductionPercent)
        assertEquals(5, idlePacket.zones.size)

        // 2. Fast attack on hot transient triggers TRANSIENT_EDGE_STROBE on first frame
        val strobePacket = mapper.mapToGlyphPulsePattern(-6f, -9f, 0.2f)
        assertEquals(GlyphPulseType.TRANSIENT_EDGE_STROBE, strobePacket.pulseType)
        assertTrue(strobePacket.rmsPercent in 78..100)
        assertTrue(strobePacket.zones.all { it.intensity0To100 in 0..100 })

        // 3. Drop signal to silence -> exponential decay holds intermediate RMS rather than snapping to 0
        val decayFrame1 = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        val decayFrame2 = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        assertTrue(decayFrame1.rmsPercent in 40 until strobePacket.rmsPercent)
        assertTrue(decayFrame2.rmsPercent in 20 until decayFrame1.rmsPercent)

        // 4. Reset clears smoothed envelopes back to 0 / IDLE_HEARTBEAT
        mapper.reset()
        val afterReset = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        assertEquals(0, afterReset.rmsPercent)
        assertEquals(0, afterReset.gainReductionPercent)
        assertEquals(GlyphPulseType.IDLE_HEARTBEAT, afterReset.pulseType)

        // 5. Dual-zone routing: C-Ring (C1) tracks RMS loudness, USB-C Clamp Bar (D1) tracks Gain Reduction
        val clampPacket = mapper.mapToGlyphPulsePattern(-14f, -14f, 4.2f)
        assertEquals(GlyphPulseType.GR_CLAMP_PULSE, clampPacket.pulseType)
        val cRingZone = clampPacket.zones.first { it.zoneId == "ZONE_C1" }
        val usbClampZone = clampPacket.zones.first { it.zoneId == "ZONE_D1" }
        assertEquals(clampPacket.rmsPercent, cRingZone.intensity0To100)
        assertEquals(clampPacket.gainReductionPercent, usbClampZone.intensity0To100)
        assertTrue(usbClampZone.activePulse)
        assertTrue(clampPacket.gainReductionPercent in 55..75)

        // 6. Gain Reduction decays smoothly over subsequent quiet limiter frames
        val grDecay1 = mapper.mapToGlyphPulsePattern(-14f, -14f, 0f)
        assertTrue(grDecay1.gainReductionPercent in 35 until clampPacket.gainReductionPercent)
    }

    @Test
    fun `Audio Core Bus latency monitor tracks System nanoTime EMA, idle baseline, and 0ms bypass`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val processor = AudioProcessor(scope = scope)

        val eq = BiquadEqHook()
        val limiter = TruePeakLimiterHook(oversamplingFactor = OversamplingFactor.X4)
        processor.registerHook(eq)
        processor.registerHook(limiter)

        // 1. Paused baseline sums active hook baseline estimates
        val initialLatency = processor.telemetry.value.dspLatencyMs
        assertEquals(eq.baselineLatencyMs + limiter.baselineLatencyMs, initialLatency, 0.001f)
        assertFalse(processor.telemetry.value.isBypassed)

        // 2. Disabling a hook lowers paused baseline immediately and zeros its snapshot latency
        processor.setHookEnabled(eq.id, false)
        assertEquals(limiter.baselineLatencyMs, processor.telemetry.value.dspLatencyMs, 0.001f)
        assertEquals(0f, processor.hookSnapshots.value.first { it.id == eq.id }.latencyMs, 0.0001f)

        // Re-enable both hooks and verify System.nanoTime() EMA measurement on processThroughHooks
        processor.setHookEnabled(eq.id, true)
        val l = FloatArray(1024) { 0.4f }
        val r = FloatArray(1024) { -0.4f }
        processor.processThroughHooks(l, r, 1024)
        val measured1 = processor.telemetry.value.dspLatencyMs
        assertTrue(measured1.isFinite() && measured1 > 0f)
        assertTrue(processor.hookSnapshots.value.all { it.latencyMs > 0f })

        processor.processThroughHooks(l, r, 1024)
        val measured2 = processor.telemetry.value.dspLatencyMs
        assertTrue(measured2.isFinite() && measured2 > 0f)

        // 3. Bypass mode forces 0.00 ms telemetry & 0.00 ms hook snapshots
        processor.setWetEnabled(false)
        assertTrue(processor.telemetry.value.isBypassed)
        assertEquals(0f, processor.telemetry.value.dspLatencyMs, 0.0001f)
        assertTrue(processor.hookSnapshots.value.all { it.latencyMs == 0f })

        // 4. Re-enabling wet DSP and pausing restores cumulative active baseline estimate
        processor.setWetEnabled(true)
        processor.pause()
        assertFalse(processor.telemetry.value.isBypassed)
        assertEquals(eq.baselineLatencyMs + limiter.baselineLatencyMs, processor.telemetry.value.dspLatencyMs, 0.001f)

        processor.release()
        scope.cancel()
    }

    @Test
    fun `smart genre recognition classifies audio styles and provisions adaptive DSP presets`() {
        // 1. Studio reference classifies as ELECTRONIC with wide stereo and 96kHz 4x oversampling
        val studioRef = AudioDecoder.generateStudioReferenceTrack()
        assertEquals(MusicGenre.ELECTRONIC, studioRef.info.diagnostic.detectedGenre)
        assertEquals(96, studioRef.info.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X4, studioRef.info.diagnostic.detectedGenre.oversampling)
        assertTrue(studioRef.info.diagnostic.genreConfidencePct >= 85)

        // 2. High-dynamic-range classical signal with high crest factor
        val classicalPcm = FloatArray(4800) { i ->
            if (i % 600 == 0) 0.95f else (kotlin.math.sin(2.0 * Math.PI * 440.0 * i / 48000.0).toFloat() * 0.05f)
        }
        val classicalInfo = AudioDecoder.analyzeBuffer(
            id = "symphony_01",
            title = "ORCHESTRAL_CONCERTO.wav",
            sourceLabel = "LOCAL SAF IMPORT",
            left = classicalPcm,
            right = classicalPcm.copyOf(),
            sampleRate = 48000,
            channels = 2,
            isBundled = false
        )
        assertEquals(MusicGenre.CLASSICAL, classicalInfo.diagnostic.detectedGenre)
        assertEquals(192, classicalInfo.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X8, classicalInfo.diagnostic.detectedGenre.oversampling)
        assertEquals(-1.2f, classicalInfo.diagnostic.detectedGenre.ceilingDb, 0.001f)
        assertTrue(classicalInfo.diagnostic.genreConfidencePct >= 85)

        // 3. Acoustic speech/vocal signal
        val acousticPcm = FloatArray(4800) { i ->
            kotlin.math.sin(2.0 * Math.PI * 330.0 * i / 48000.0).toFloat() * 0.22f
        }
        val speechInfo = AudioDecoder.analyzeBuffer(
            id = "acoustic_guitar",
            title = "ACOUSTIC_SESSION.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = acousticPcm,
            right = acousticPcm.copyOf(),
            sampleRate = 48000,
            channels = 1,
            isBundled = false
        )
        assertEquals(MusicGenre.ACOUSTIC, speechInfo.diagnostic.detectedGenre)
        assertEquals(192, speechInfo.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X8, speechInfo.diagnostic.detectedGenre.oversampling)
        assertEquals(-1.0f, speechInfo.diagnostic.detectedGenre.ceilingDb, 0.001f)

        // 4. Verify all 9 genres have complete tuning parameters and target LUFS
        assertEquals(9, MusicGenre.entries.size)
        MusicGenre.entries.forEach { genre ->
            assertTrue(genre.targetKhz in listOf(96, 192))
            assertTrue(genre.oversampling in listOf(OversamplingFactor.X4, OversamplingFactor.X8))
            assertTrue(genre.ceilingDb in -1.5f..-0.3f)
            assertTrue(genre.drive in 0.1f..0.8f)
            assertTrue(genre.punch in 0.3f..0.95f)
            assertTrue(genre.air in 0.2f..0.9f)
            assertEquals(5, genre.eqOffsetsDb.size)
            assertTrue(genre.targetLufs in -19.0f..-7.0f)
            if (genre in listOf(MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.ROCK, MusicGenre.HIP_HOP)) {
                assertEquals(-14.0f, genre.targetLufs, 0.001f)
            }
        }
        assertEquals(-8.5f, MusicGenre.DRILL.targetLufs, 0.001f)
        assertEquals(-10.0f, MusicGenre.TECHNO.targetLufs, 0.001f)
        assertEquals(-9.0f, MusicGenre.TRAP.targetLufs, 0.001f)
    }
}
