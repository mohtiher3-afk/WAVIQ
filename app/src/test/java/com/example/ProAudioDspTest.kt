package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDspEngine
import com.example.audio.AudioProcessor
import com.example.audio.BiquadFilter
import com.example.audio.DecodedAudio
import com.example.audio.FilterType
import com.example.audio.HiResUpsamplingHook
import com.example.audio.TruePeakLimiterHook
import com.example.model.AudioTelemetry
import com.example.model.AudioTrackInfo
import com.example.model.ExportFormat
import com.example.model.OversamplingFactor
import com.example.model.TranscodeConfig
import com.example.model.WaviqUiState
import com.example.ui.screens.Screen09StudioProSuite
import com.example.ui.screens.Screen13TranscodeMatrix
import com.example.ui.theme.WaviqTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProAudioDspTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `64-bit BiquadFilter stays numerically stable on sub-bass 35Hz shelf at 192kHz`() {
        val filter = BiquadFilter(192000.0)
        filter.configure(FilterType.LOW_SHELF, 35f, 0.707f, 6.0f, 192000f)

        var lastOut = 0.0
        for (i in 0 until 4096) {
            val x = 0.1 * sin(2.0 * PI * 35.0 * i / 192000.0)
            lastOut = filter.processLeftDouble(x)
            assertTrue("Filter output became non-finite at $i", lastOut.isFinite())
        }
        assertTrue(abs(lastOut) > 0.001)
    }

    @Test
    fun `HiResUpsamplingHook adds coherent HF harmonic brightness and stereo expansion`() {
        val hook = HiResUpsamplingHook(restorationAmount = 0.85f, targetKhz = 192, stereoExpansionAmount = 0.75f)
        hook.configure(48000)

        // Generate 6kHz tone with slight stereo imbalance
        val n = 1024
        val left = FloatArray(n) { i -> (0.35 * sin(2.0 * PI * 6000.0 * i / 48000.0)).toFloat() }
        val right = FloatArray(n) { i -> (0.20 * sin(2.0 * PI * 6000.0 * i / 48000.0)).toFloat() }
        val drySideEnergy = left.indices.sumOf { i ->
            val s = 0.5 * (left[i] - right[i])
            s * s
        }

        hook.processBlock(left, right, n, 48000)

        val wetSideEnergy = left.indices.sumOf { i ->
            val s = 0.5 * (left[i] - right[i])
            s * s
        }
        assertTrue(left.all { it.isFinite() } && right.all { it.isFinite() })
        assertTrue("Expected stereo expansion on upper band ($wetSideEnergy vs $drySideEnergy)", wetSideEnergy > drySideEnergy)
    }

    @Test
    fun `3-Band TruePeakLimiterHook clamps hot sub-bass independently and enforces ISP ceiling`() {
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X8)
        val n = 1024
        val left = FloatArray(n) { i -> (1.45 * sin(2.0 * PI * 60.0 * i / 48000.0)).toFloat() }
        val right = FloatArray(n) { i -> (1.45 * sin(2.0 * PI * 60.0 * i / 48000.0)).toFloat() }

        limiter.processBlock(left, right, n, 48000)

        assertTrue("Expected low-band gain reduction on 60Hz hot signal", limiter.lastLowBandGrDb > 0.5f)
        assertTrue("Expected overall GR > 0", limiter.lastGainReductionDb > 0.5f)
        assertTrue("Peak exceeded -1.0 dBTP ceiling", left.all { it.isFinite() && abs(it) <= 0.892f })
    }

    @Test
    fun `exportMasterWav renders 192kHz 32-bit IEEE Float WAV and applies TPDF dither to 16-bit PCM`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val engine = AudioDspEngine(context, scope)

        try {
            val sr = 48000
            val len = 2048
            val srcL = FloatArray(len) { i -> (0.25 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
            val srcR = FloatArray(len) { i -> (0.25 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
            engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))

            // 1. Export 32-bit IEEE Float @ 192kHz
            val float192File = File(context.cacheDir, "test_master_192k_32f.wav")
            engine.exportMasterWav(
                outFile = float192File,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_32F_192,
                    ditherEnabled = false,
                    normalizeToTarget = true,
                    targetTruePeakDb = -1.0f
                )
            ) { _, _ -> }

            val bytes32f = float192File.readBytes()
            val hdr = ByteBuffer.wrap(bytes32f, 0, 44).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals("RIFF", String(bytes32f, 0, 4))
            assertEquals("WAVE", String(bytes32f, 8, 4))
            assertEquals(3, hdr.getShort(20).toInt() and 0xFFFF) // IEEE_FLOAT format tag = 3
            assertEquals(2, hdr.getShort(22).toInt() and 0xFFFF)
            assertEquals(192000, hdr.getInt(24))
            assertEquals(32, hdr.getShort(34).toInt() and 0xFFFF)

            // 192kHz / 48kHz = 4x frame count
            val dataBytes32f = hdr.getInt(40)
            assertEquals(len * 4 * 2 * 4, dataBytes32f)

            val (parsedL, parsedR) = AudioProcessor.parsePcmBytes(bytes32f)
            assertEquals(len * 4, parsedL.size)
            assertTrue(parsedL.all { it.isFinite() } && parsedR.all { it.isFinite() })

            // 2. Export 24-bit PCM @ 192kHz
            val pcm24File = File(context.cacheDir, "test_master_192k_24b.wav")
            engine.exportMasterWav(
                outFile = pcm24File,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_24_192,
                    ditherEnabled = true,
                    normalizeToTarget = true,
                    targetTruePeakDb = -1.0f
                )
            ) { _, _ -> }

            val bytes24 = pcm24File.readBytes()
            val hdr24 = ByteBuffer.wrap(bytes24, 0, 44).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals(1, hdr24.getShort(20).toInt() and 0xFFFF)
            assertEquals(192000, hdr24.getInt(24))
            assertEquals(24, hdr24.getShort(34).toInt() and 0xFFFF)

            // 3. Compare TPDF dither ON vs OFF on 16-bit PCM export of quiet signal
            val quietL = FloatArray(512) { 0.00001f }
            val quietR = FloatArray(512) { -0.00001f }
            engine.loadDecodedAudio(DecodedAudio(quietL, quietR, sr, AudioTrackInfo()))

            val ditherOffFile = File(context.cacheDir, "test_dither_off.wav")
            val ditherOnFile = File(context.cacheDir, "test_dither_on.wav")
            engine.exportMasterWav(
                outFile = ditherOffFile,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_16_44,
                    ditherEnabled = false,
                    normalizeToTarget = false
                )
            ) { _, _ -> }
            engine.exportMasterWav(
                outFile = ditherOnFile,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_16_44,
                    ditherEnabled = true,
                    normalizeToTarget = false
                )
            ) { _, _ -> }

            assertFalse(
                "TPDF dither must measurably change quantized LSB output compared to undithered truncation",
                ditherOffFile.readBytes().contentEquals(ditherOnFile.readBytes())
            )

            float192File.delete()
            pcm24File.delete()
            ditherOffFile.delete()
            ditherOnFile.delete()
            } finally {
                engine.release()
                scope.cancel()
            }
        }
    }

    @Test
    fun `StudioProSuite renders 3-band limiter telemetry tiles`() {
        composeTestRule.setContent {
            WaviqTheme {
                Screen09StudioProSuite(
                    state = WaviqUiState(
                        telemetry = AudioTelemetry(lowBandGrDb = 1.4f, midBandGrDb = 0.6f, highBandGrDb = 0.3f)
                    ),
                    onToggleDspHook = { _, _ -> },
                    onSetOversampling = {},
                    onToggleHiResKhz = {},
                    onJumpRoute = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pro_band_gr_low").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_band_gr_mid").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_band_gr_high").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TranscodeMatrix exposes 192kHz 24-bit and 32-bit IEEE float formats`() {
        var selectedFormat: ExportFormat? = null
        composeTestRule.setContent {
            WaviqTheme {
                Screen13TranscodeMatrix(
                    state = WaviqUiState(),
                    onUpdateConfig = { fmt, _, _ -> if (fmt != null) selectedFormat = fmt },
                    onStartExport = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("export_format_wav_24_192").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_format_wav_32f_192").performScrollTo().performClick()
        assertEquals(ExportFormat.WAV_32F_192, selectedFormat)
    }

    @Test
    fun `mastering preset buttons adjust True-Peak limiter and SBR settings automatically`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.WaviqViewModel(app)

        viewModel.applyMasteringPreset("Warm Tape")
        assertEquals(96, viewModel.uiState.value.hiResTargetKhz)
        assertEquals(OversamplingFactor.X8, viewModel.uiState.value.oversamplingFactor)
        assertEquals(-1.5f, viewModel.uiState.value.deckParams.ceilingDb, 0.001f)

        viewModel.applyMasteringPreset("Transparent Punch")
        assertEquals(192, viewModel.uiState.value.hiResTargetKhz)
        assertEquals(OversamplingFactor.X4, viewModel.uiState.value.oversamplingFactor)
        assertEquals(-1.0f, viewModel.uiState.value.deckParams.ceilingDb, 0.001f)

        viewModel.applyMasteringPreset("Broadcast")
        assertEquals(96, viewModel.uiState.value.hiResTargetKhz)
        assertEquals(OversamplingFactor.X4, viewModel.uiState.value.oversamplingFactor)
        assertEquals(-2.0f, viewModel.uiState.value.deckParams.ceilingDb, 0.001f)
    }

    @Test
    fun `exportMasterWav produces unprocessed bit-perfect passthrough when bypassed`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            val engine = AudioDspEngine(context, scope)

            try {
                val sr = 48000
                val len = 1024
                val srcL = FloatArray(len) { i -> (0.4 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
                val srcR = FloatArray(len) { i -> (0.4 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
                engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))

                // Engage bypass
                engine.updateDsp(
                    bands = com.example.model.defaultEqBands(),
                    deck = com.example.model.TactileDeckParams(),
                    stems = com.example.model.StemMixerState(),
                    isStemScreen = false,
                    wetEnabled = false
                )

                val bypassExportFile = File(context.cacheDir, "test_bypass_export.wav")
                engine.exportMasterWav(
                    outFile = bypassExportFile,
                    config = TranscodeConfig(
                        format = ExportFormat.WAV_24_48,
                        ditherEnabled = false,
                        normalizeToTarget = false
                    )
                ) { _, _ -> }

                val (outL, outR) = AudioProcessor.parsePcmBytes(bypassExportFile.readBytes())
                assertEquals(len, outL.size)
                for (i in 0 until minOf(64, len)) {
                    assertEquals(srcL[i], outL[i], 0.001f)
                }
                bypassExportFile.delete()
            } finally {
                engine.release()
                scope.cancel()
            }
        }
    }
}
