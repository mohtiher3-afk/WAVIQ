package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDspEngine
import com.example.audio.BiquadEqHook
import com.example.audio.Dsp64BusEngine
import com.example.audio.GainTrimHook
import com.example.audio.HiResUpsamplingHook
import com.example.audio.Sbr192KhzRestorationModule
import com.example.audio.ThreeBandTruePeakLimiter
import com.example.audio.TruePeakLimiterHook
import com.example.audio.Waviq64BitDspPipeline
import com.example.model.ExportFormat
import com.example.model.OversamplingFactor
import com.example.model.TranscodeConfig
import com.example.model.defaultEqBands
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Dsp64CoreInterfacesTest {

    @Test
    fun `Sbr192KhzRestorationModule configures 192kHz SBR metrics and synthesizes 64-bit HF shimmer`() {
        val sbr: Sbr192KhzRestorationModule = HiResUpsamplingHook()
        sbr.configure(
            sampleRate = 192000,
            targetKhz = 192,
            restorationAmount = 0.85f,
            stereoExpansionAmount = 0.72f
        )

        val metrics = sbr.readSbrMetrics()
        assertEquals(192, metrics.targetKhz)
        assertEquals(192000, metrics.configuredSampleRateHz)
        assertTrue(metrics.is192KhzUltraMode)
        assertEquals(4500f, metrics.hfCrossoverHz, 0.01f)

        val n = 2048
        val left = DoubleArray(n) { i -> 0.4 * sin(2.0 * PI * 7200.0 * i / 192000.0) }
        val right = DoubleArray(n) { i -> 0.25 * sin(2.0 * PI * 7200.0 * i / 192000.0) }
        val rawPeak = left.maxOf { abs(it) }

        sbr.processDoubleBlock(left, right, n, 192000)

        assertTrue("Expected finite 64-bit output", left.all { it.isFinite() } && right.all { it.isFinite() })
        val processedPeak = left.maxOf { abs(it) }
        assertTrue("Expected SBR harmonic shelf to alter HF envelope ($rawPeak vs $processedPeak)", processedPeak > rawPeak * 1.02)

        sbr.reset()
        assertEquals(192, sbr.readSbrMetrics().targetKhz)
    }

    @Test
    fun `ThreeBandTruePeakLimiter enforces ISP ceiling and reports independent 3-band telemetry`() {
        val limiter: ThreeBandTruePeakLimiter = TruePeakLimiterHook()
        limiter.configure(
            ceilingDb = -1.0f,
            oversamplingFactor = OversamplingFactor.X8,
            sampleRate = 96000
        )

        assertEquals(250f, limiter.lowCrossoverHz, 0.01f)
        assertEquals(4000f, limiter.highCrossoverHz, 0.01f)

        val n = 2048
        val left = DoubleArray(n) { i ->
            0.95 * sin(2.0 * PI * 70.0 * i / 96000.0) +
                0.75 * sin(2.0 * PI * 6800.0 * i / 96000.0)
        }
        val right = left.copyOf()

        limiter.processDoubleBlock(left, right, n, 96000)

        val ceilingAmp = 10.0.pow(-1.0 / 20.0)
        val maxSample = maxOf(left.maxOf { abs(it) }, right.maxOf { abs(it) })
        assertTrue("Limiter output ($maxSample) must stay within -1.0 dBTP ($ceilingAmp)", maxSample <= ceilingAmp + 1e-4)

        val tel = limiter.readLimiterTelemetry()
        assertEquals(-1.0f, tel.ceilingDb, 0.001f)
        assertEquals(OversamplingFactor.X8, tel.oversamplingFactor)
        assertTrue("Expected positive gain reduction telemetry, got ${tel.masterGainReductionDb}", tel.masterGainReductionDb > 0.2f)
        assertTrue("Expected independent low-band GR, got ${tel.lowBandGrDb}", tel.lowBandGrDb > 0f)

        limiter.reset()
        assertEquals(0f, limiter.readLimiterTelemetry().masterGainReductionDb, 0.001f)
    }

    @Test
    fun `Waviq64BitDspPipeline preserves 64-bit internal headroom across chained stages without intermediate clipping`() {
        val boostStage = GainTrimHook(id = "boost", gainLinear = 4.0f)
        val eqStage = BiquadEqHook().apply { configure(defaultEqBands(), 48000) }
        val trimStage = GainTrimHook(id = "trim", gainLinear = 0.25f)
        val sbr = HiResUpsamplingHook(restorationAmount = 0f)
        val limiter = TruePeakLimiterHook(ceilingDb = -0.5f, oversamplingFactor = OversamplingFactor.X4)
        limiter.isEnabled = false

        val pipeline: Dsp64BusEngine = Waviq64BitDspPipeline(
            stages = listOf(boostStage, eqStage, trimStage, sbr, limiter),
            sbrModule = sbr,
            truePeakLimiter = limiter
        )

        val n = 512
        val inputL = DoubleArray(n) { i -> 0.5 * sin(2.0 * PI * 1000.0 * i / 48000.0) }
        val inputR = inputL.copyOf()

        pipeline.processChainDouble(inputL, inputR, n, 48000)

        // boost(4.0) * 0.5 = 2.0 internally (>1.0) followed by trim(0.25) -> stays smooth sine without clipping!
        assertTrue("64-bit intermediate headroom preserved", inputL.all { it.isFinite() })
        val peak = inputL.maxOf { abs(it) }
        assertTrue("Expected reconstructed amplitude near 0.5..0.6 (not clipped to 0.25), got $peak", peak > 0.42 && peak < 0.75)

        val floatL = FloatArray(n) { i -> (0.3 * sin(2.0 * PI * 440.0 * i / 48000.0)).toFloat() }
        val floatR = floatL.copyOf()
        pipeline.processChainFloat(floatL, floatR, n, 48000)
        assertTrue(floatL.all { it.isFinite() })

        pipeline.resetAll()
    }

    @Test
    fun `AudioDspEngine exposes typed 64-bit SBR and Limiter interfaces and offline 64-bit pipeline`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val engine = AudioDspEngine(context, scope)
            engine.setHiResTargetKhz(192)
            engine.setHiResRestorationAmount(0.8f)
            engine.setHiResStereoExpansionAmount(0.6f)
            engine.setOversamplingFactor(OversamplingFactor.X8)

            val sbr: Sbr192KhzRestorationModule = engine.sbrRestorationModule
            val limiter: ThreeBandTruePeakLimiter = engine.threeBandTruePeakLimiter

            assertEquals(192, sbr.targetKhz)
            assertEquals(OversamplingFactor.X8, limiter.oversamplingFactor)
            assertTrue(engine.getSbrMetrics().is192KhzUltraMode)
            assertEquals(OversamplingFactor.X8, engine.getLimiterTelemetry().oversamplingFactor)
            assertEquals(6, engine.dsp64Pipeline.stages.size)

            val (offlineBus, offTflite) = engine.createOfflineDsp64Pipeline(
                internalSampleRate = 48000,
                config = TranscodeConfig(format = ExportFormat.WAV_24_192, normalizeToTarget = true, targetTruePeakDb = -1.0f)
            )
            assertEquals(OversamplingFactor.X8, offlineBus.truePeakLimiter.oversamplingFactor)
            assertFalse(offlineBus.stages.isEmpty())
            offTflite.close()
            engine.release()
        } finally {
            scope.cancel()
        }
    }
}
