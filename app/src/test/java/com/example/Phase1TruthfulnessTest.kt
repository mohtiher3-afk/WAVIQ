package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDecoder
import com.example.audio.AudioDspEngine
import com.example.audio.AudioMeasurement
import com.example.audio.AudioProcessor
import com.example.audio.DecodedAudio
import com.example.model.AudioTrackInfo
import com.example.model.ExportFormat
import com.example.model.OversamplingFactor
import com.example.model.StemChannelState
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import com.example.model.TranscodeConfig
import com.example.model.WaviqUiState
import com.example.model.defaultEqBands
import com.example.ui.screens.UnifiedLibraryScreen
import com.example.ui.theme.WaviqTheme
import com.example.viewmodel.WaviqViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase1TruthfulnessTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `AudioMeasurement handles silence, NaNs, short buffers, and BS1770 K-weighting`() {
        val sr = 48000
        // 1. Silence & non-finite safety
        val silent = FloatArray(sr)
        assertEquals(-70.0f, AudioMeasurement.measureIntegratedLufs(silent, silent, sr), 0.01f)
        assertEquals(-96.0f, AudioMeasurement.measureTruePeakDb(silent, silent), 0.01f)
        assertEquals(-96.0f, AudioMeasurement.measureSamplePeakDb(silent, silent), 0.01f)

        val nonFinite = FloatArray(1024) { if (it % 2 == 0) Float.NaN else Float.POSITIVE_INFINITY }
        assertTrue(AudioMeasurement.measureIntegratedLufs(nonFinite, nonFinite, sr).isFinite())
        assertTrue(AudioMeasurement.measureTruePeakDb(nonFinite, nonFinite).isFinite())

        // 2. Short <400ms buffer fallback
        val shortLen = (sr * 0.2).toInt()
        val shortL = FloatArray(shortLen) { i -> (0.5 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
        val shortLufs = AudioMeasurement.measureIntegratedLufs(shortL, shortL, sr)
        assertTrue("Short buffer LUFS should be active around -7 LUFS, got $shortLufs", shortLufs > -20f && shortLufs < 0f)

        // 3. K-weighting Stage 1 high-shelf boosts 4kHz vs equal-amplitude 100Hz
        val oneSec = sr
        val low100 = FloatArray(oneSec) { i -> (0.25 * sin(2.0 * PI * 100.0 * i / sr)).toFloat() }
        val high4k = FloatArray(oneSec) { i -> (0.25 * sin(2.0 * PI * 4000.0 * i / sr)).toFloat() }
        val lowLufs = AudioMeasurement.measureIntegratedLufs(low100, low100, sr)
        val highLufs = AudioMeasurement.measureIntegratedLufs(high4k, high4k, sr)
        assertTrue("Expected 4kHz K-weighted LUFS ($highLufs) > 100Hz ($lowLufs)", highLufs > lowLufs + 2.5f)

        // 4. Dual-stage gating excludes long quiet tail
        val activeOnly = FloatArray(sr) { i -> (0.5 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
        val gatedMix = FloatArray(sr * 4) { i ->
            if (i < sr) (0.5 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() else 0.00001f
        }
        val activeLufs = AudioMeasurement.measureIntegratedLufs(activeOnly, activeOnly, sr)
        val gatedLufs = AudioMeasurement.measureIntegratedLufs(gatedMix, gatedMix, sr)
        assertEquals("Gating should keep active loudness close to active block", activeLufs, gatedLufs, 1.0f)
    }

    @Test
    fun `AudioMeasurement catches inter-sample true peaks exceeding sample peaks`() {
        val sr = 48000
        // Fs/4 sine shifted by PI/4 samples hits exactly +/-0.85 * sin(PI/4) = ~0.601 at sample points,
        // while the analog waveform reaches 0.85 between samples.
        val len = 512
        val sig = FloatArray(len) { n ->
            (0.85 * sin(2.0 * PI * (sr / 4.0) * n / sr + PI / 4.0)).toFloat()
        }
        val samplePeak = AudioMeasurement.measureSamplePeakDb(sig, sig)
        val truePeak = AudioMeasurement.measureTruePeakDb(sig, sig)
        assertTrue(
            "Expected True Peak ($truePeak dBTP) > Sample Peak ($samplePeak dBFS)",
            truePeak > samplePeak + 1.5f
        )
    }

    @Test
    fun `AudioMeasurement anti-aliased downsampler attenuates ultrasonic alias tones cleanly`() {
        val srcSr = 96000
        val dstSr = 48000
        val len = 9600 // 100ms
        val inBand = FloatArray(len) { i -> (0.5 * sin(2.0 * PI * 1000.0 * i / srcSr)).toFloat() }
        val ultrasonic = FloatArray(len) { i -> (0.5 * sin(2.0 * PI * 36000.0 * i / srcSr)).toFloat() }

        val (outInL, _) = AudioMeasurement.resampleWithAntiAliasing(inBand, inBand, srcSr, dstSr)
        val (outUltraL, _) = AudioMeasurement.resampleWithAntiAliasing(ultrasonic, ultrasonic, srcSr, dstSr)

        fun rms(arr: FloatArray, skip: Int): Double {
            var s = 0.0
            var c = 0
            for (i in skip until arr.size) {
                s += arr[i] * arr[i]
                c++
            }
            return sqrt((s / c.coerceAtLeast(1)).coerceAtLeast(1e-12))
        }

        val inRms = rms(outInL, 256)
        val ultraRms = rms(outUltraL, 256)
        assertTrue("1kHz signal should remain strong ($inRms)", inRms > 0.30)
        assertTrue("36kHz ultrasonic signal must be heavily attenuated before decimation ($ultraRms)", ultraRms < 0.03)
    }

    @Test
    fun `exportMasterWav honors Band Mixer mute and matches live hook pipeline`() {
        runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val engine = AudioDspEngine(context, scope)
        try {
            val sr = 48000
            val len = 4096
            val srcL = FloatArray(len) { i -> (0.45 * sin(2.0 * PI * 80.0 * i / sr)).toFloat() }
            val srcR = FloatArray(len) { i -> (0.45 * sin(2.0 * PI * 80.0 * i / sr)).toFloat() }
            engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))

            val deck = TactileDeckParams(drive = 0.25f, width = 0.65f, punch = 0.55f, air = 0.50f, ceilingDb = -1.0f)
            val bands = defaultEqBands()
            val cfg = TranscodeConfig(
                format = ExportFormat.WAV_24_48,
                ditherEnabled = false,
                normalizeToTarget = false
            )

            // 1. Export with Bass band active
            engine.updateDsp(bands, deck, StemMixerState(), isStemScreen = true, wetEnabled = true)
            val unmutedFile = File(context.cacheDir, "parity_unmuted.wav")
            engine.exportMasterWav(unmutedFile, cfg) { _, _ -> }
            val (unmutedL, _) = AudioProcessor.parsePcmBytes(unmutedFile.readBytes())

            // 2. Export with Bass band muted on Band Mixer
            val mutedBassStems = StemMixerState(
                bass = StemChannelState("bass", "SUB / BASS", "< 220Hz", level = 1.0f, muted = true)
            )
            engine.updateDsp(bands, deck, mutedBassStems, isStemScreen = true, wetEnabled = true)
            val mutedFile = File(context.cacheDir, "parity_muted.wav")
            engine.exportMasterWav(mutedFile, cfg) { _, _ -> }
            val (mutedL, _) = AudioProcessor.parsePcmBytes(mutedFile.readBytes())

            val unmutedEnergy = unmutedL.sumOf { (it * it).toDouble() }
            val mutedEnergy = mutedL.sumOf { (it * it).toDouble() }
            assertTrue(
                "Muting <220Hz band in Band Mixer must strongly reduce 80Hz energy in exported WAV ($mutedEnergy vs $unmutedEnergy)",
                mutedEnergy < unmutedEnergy * 0.20
            )

            // 3. Compare exportMasterWav against live audioProcessor hook pass at 48kHz
            engine.updateDsp(bands, deck, StemMixerState(), isStemScreen = false, wetEnabled = true)
            val parityFile = File(context.cacheDir, "parity_live_match.wav")
            engine.exportMasterWav(parityFile, cfg) { _, _ -> }
            val (expL, _) = AudioProcessor.parsePcmBytes(parityFile.readBytes())

            // Reset hook filters so both paths start from clean filter state on identical blocks
            engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))
            engine.updateDsp(bands, deck, StemMixerState(), isStemScreen = false, wetEnabled = true)
            val liveL = FloatArray(len) { srcL[it] }
            val liveR = FloatArray(len) { srcR[it] }
            val blkL = FloatArray(1024)
            val blkR = FloatArray(1024)
            for (pos in 0 until len step 1024) {
                System.arraycopy(liveL, pos, blkL, 0, 1024)
                System.arraycopy(liveR, pos, blkR, 0, 1024)
                engine.audioProcessor.processThroughHooks(blkL, blkR, 1024)
                System.arraycopy(blkL, 0, liveL, pos, 1024)
                System.arraycopy(blkR, 0, liveR, pos, 1024)
            }

            var maxDiff = 0f
            for (i in 0 until len) {
                val d = abs(expL[i] - liveL[i])
                if (d > maxDiff) maxDiff = d
            }
            assertTrue("Expected offline export and live preview hook chain parity (maxDiff=$maxDiff)", maxDiff < 0.02f)

            unmutedFile.delete()
            mutedFile.delete()
            parityFile.delete()
        } finally {
            engine.release()
            scope.cancel()
        }
        }
    }

    @Test
    fun `exportMasterWav bypasses TPDF dither cleanly on 32-bit IEEE Float exports`() {
        runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val engine = AudioDspEngine(context, scope)
        try {
            val sr = 48000
            val len = 1024
            val srcL = FloatArray(len) { i -> (0.2 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
            val srcR = FloatArray(len) { i -> (0.2 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
            engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))

            val f32DitherOff = File(context.cacheDir, "f32_off.wav")
            val f32DitherOn = File(context.cacheDir, "f32_on.wav")
            engine.exportMasterWav(
                f32DitherOff,
                TranscodeConfig(ExportFormat.WAV_32F_192, ditherEnabled = false, normalizeToTarget = false)
            ) { _, _ -> }
            engine.exportMasterWav(
                f32DitherOn,
                TranscodeConfig(ExportFormat.WAV_32F_192, ditherEnabled = true, normalizeToTarget = false)
            ) { _, _ -> }

            assertArrayEquals(
                "32-bit IEEE Float WAV bytes must be identical whether ditherEnabled is true or false",
                f32DitherOff.readBytes(),
                f32DitherOn.readBytes()
            )
            f32DitherOff.delete()
            f32DitherOn.delete()
        } finally {
            engine.release()
            scope.cancel()
        }
        }
    }

    @Test
    fun `AudioDecoder analyzes 120-second track beyond old 90s cap without truncation`() {
        val sr = 48000
        val totalFrames = sr * 120
        val left = FloatArray(totalFrames) { i -> (0.3 * sin(2.0 * PI * 220.0 * i / sr)).toFloat() }
        val right = FloatArray(totalFrames) { i -> (0.3 * sin(2.0 * PI * 220.0 * i / sr)).toFloat() }

        val info = AudioDecoder.analyzeBuffer(
            id = "long_120s",
            title = "LONG_SESSION_MASTER.wav",
            sourceLabel = "LOCAL TEST",
            left = left,
            right = right,
            sampleRate = sr,
            channels = 2,
            isBundled = false,
            isDurationTruncated = false
        )

        assertEquals(120_000L, info.durationMs)
        assertFalse(info.isDurationTruncated)
        assertTrue(info.diagnostic.isHeuristicEstimate)
        assertTrue(info.integratedLufs.isFinite() && info.integratedLufs > -30f)
        assertTrue(info.truePeakDb.isFinite() && info.truePeakDb > -15f)
    }

    @Test
    fun `WaviqViewModel rejects unsupported commands without mutating EQ or ceiling and UnifiedLibraryScreen disables dither on 32f`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = WaviqViewModel(app)
        val eqBefore = vm.uiState.value.eqBands.map { it.gainDb }
        val deckBefore = vm.uiState.value.deckParams

        vm.submitAiCommand("gibberish unknown command xyz123")
        val after = vm.uiState.value
        assertTrue(after.lastCommandUnsupported)
        assertEquals(0, after.aiConfidencePct)
        assertEquals(eqBefore, after.eqBands.map { it.gainDb })
        assertEquals(deckBefore, after.deckParams)

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedLibraryScreen(
                    state = WaviqUiState(
                        transcodeConfig = TranscodeConfig(
                            format = ExportFormat.WAV_32F_192,
                            ditherEnabled = true,
                            normalizeToTarget = true,
                            targetTruePeakDb = -1.5f
                        )
                    ),
                    savedMasters = emptyList(),
                    onUpdateConfig = { _, _, _ -> },
                    onStartExport = {},
                    onSaveToDeviceClick = {},
                    onTogglePlay = {},
                    onSeek = {},
                    onToggleAb = {},
                    onLoadSavedMaster = {},
                    onDeleteSavedMaster = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("transcode_dither_switch").performScrollTo().assertIsNotEnabled()
    }
}
