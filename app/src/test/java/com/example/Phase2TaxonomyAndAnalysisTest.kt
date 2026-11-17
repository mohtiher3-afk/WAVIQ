package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioAnalysisService
import com.example.audio.AudioDecoder
import com.example.audio.MusicTaxonomyRepository
import com.example.audio.ScoredTaxonomyTag
import com.example.model.AudioAnalysisReport
import com.example.model.AudioTechnicalFinding
import com.example.model.AudioTrackInfo
import com.example.model.FindingSeverity
import com.example.model.MusicGenre
import com.example.model.ProTuningTab
import com.example.model.RhythmEstimate
import com.example.model.TrackDiagnosticReport
import com.example.model.VocalActivityEstimate
import com.example.model.WaviqUiState
import com.example.ui.screens.UnifiedStudioScreen
import com.example.ui.theme.WaviqTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase2TaxonomyAndAnalysisTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun resetCache() {
        MusicTaxonomyRepository.clearCacheForTest()
    }

    @Test
    fun `music_taxonomy asset loads cleanly with stable IDs, aliases, fusion links, and separated tag groups`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val taxonomy = MusicTaxonomyRepository.load(context)

        assertTrue("Taxonomy version should be >= 1", taxonomy.version >= 1)
        assertTrue("Expected at least 18 taxonomy entries, found ${taxonomy.entries.size}", taxonomy.entries.size >= 18)

        val unknown = MusicTaxonomyRepository.findById(taxonomy, MusicTaxonomyRepository.UNKNOWN_MIXED_ID)
        assertNotNull("unknown_mixed fallback entry must exist", unknown)
        assertNull("unknown_mixed must have null mappedMusicGenre", unknown!!.mappedMusicGenre)

        // Verify bilingual & fusion alias resolution
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "trapsoul").any { it.id == "trap_soul" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "ukdrill").any { it.id == "drill_uk_ny" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "دريل").any { it.id == "drill_uk_ny" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "boombap").any { it.id == "boom_bap" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "alternative r&b").any { it.id == "alternative_rnb" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "lofi").any { it.id == "ambient_lofi" })
        assertTrue(MusicTaxonomyRepository.findByAlias(taxonomy, "خليجي").any { it.id == "arabic_pop_maqam" })

        // Verify all non-unknown entries carry strictly separated mood/production/vocal/instrumentation tags
        taxonomy.entries
            .filter { it.id != MusicTaxonomyRepository.UNKNOWN_MIXED_ID }
            .forEach { entry ->
                assertTrue("${entry.id} must have moodEnergyTags", entry.moodEnergyTags.isNotEmpty())
                assertTrue("${entry.id} must have productionStyleTags", entry.productionStyleTags.isNotEmpty())
                assertTrue("${entry.id} must have vocalStyleTags", entry.vocalStyleTags.isNotEmpty())
                assertTrue("${entry.id} must have instrumentationRhythmTags", entry.instrumentationRhythmTags.isNotEmpty())
            }
    }

    @Test
    fun `AudioAnalysisService separates low vs high spectral energy and flags out-of-phase stereo risk`() {
        val sr = 48000
        val frames = sr * 2

        // 1. Sub-heavy 90Hz mono-summed stereo signal
        val subL = FloatArray(frames) { i -> (0.45 * sin(2.0 * PI * 90.0 * i / sr)).toFloat() }
        val subR = FloatArray(frames) { i -> (0.45 * sin(2.0 * PI * 90.0 * i / sr)).toFloat() }
        val subReport = AudioAnalysisService.analyze(null, subL, subR, sr, 2, "sub_test.wav")
        assertTrue("90Hz signal should show dominant lowEnergyPct (got ${subReport.lowEnergyPct}%)", subReport.lowEnergyPct >= 80)
        assertTrue("In-phase L==R should have correlation ~1.0 (got ${subReport.stereoCorrelation})", subReport.stereoCorrelation > 0.95f)
        assertFalse("In-phase L==R must not flag phase risk", subReport.hasPhaseRisk)

        // 2. Inverted L/R 8kHz bright signal (R = -L) -> highEnergyPct dominant + phase risk
        val brightL = FloatArray(frames) { i -> (0.40 * sin(2.0 * PI * 8000.0 * i / sr)).toFloat() }
        val brightR = FloatArray(frames) { i -> -brightL[i] }
        val brightReport = AudioAnalysisService.analyze(null, brightL, brightR, sr, 2, "bright_phase.wav")
        assertTrue("8kHz signal should show dominant highEnergyPct (got ${brightReport.highEnergyPct}%)", brightReport.highEnergyPct >= 75)
        assertTrue("Inverted L/R must flag phase risk", brightReport.hasPhaseRisk)
        assertTrue("Inverted correlation must be <= -0.9 (got ${brightReport.stereoCorrelation})", brightReport.stereoCorrelation <= -0.90f)
        assertTrue(
            "Expected STEREO_PHASE_RISK technical finding",
            brightReport.technicalFindings.any { it.code == "STEREO_PHASE_RISK" && it.severity == FindingSeverity.WARNING }
        )
    }

    @Test
    fun `AudioAnalysisService measures EBU R128 LRA and onset-autocorrelation BPM accurately without guessing steady tones`() {
        val sr = 48000
        val seconds = 8
        val frames = sr * seconds

        // 1. Dynamic 120 BPM pulse train (0.5s period = 24,000 samples) with loud vs quiet sections -> non-zero LRA & ~120 BPM
        val beatInterval = sr / 2 // 120 BPM
        val pulseL = FloatArray(frames) { i ->
            val phaseInBeat = i % beatInterval
            val env = exp(-phaseInBeat.toDouble() / (sr * 0.045))
            val sectionScale = if (i < frames / 2) 0.90 else 0.16
            (sectionScale * env * sin(2.0 * PI * 110.0 * i / sr) +
                0.22 * sectionScale * env * sin(2.0 * PI * 1800.0 * i / sr)).toFloat()
        }
        val pulseR = FloatArray(frames) { i ->
            val phaseInBeat = i % beatInterval
            val env = exp(-phaseInBeat.toDouble() / (sr * 0.045))
            val sectionScale = if (i < frames / 2) 0.85 else 0.14
            (sectionScale * env * sin(2.0 * PI * 110.0 * i / sr + 0.15) +
                0.22 * sectionScale * env * sin(2.0 * PI * 1800.0 * i / sr)).toFloat()
        }

        val rhythmReport = AudioAnalysisService.analyze(null, pulseL, pulseR, sr, 2, "kick_test.wav")
        assertNotNull("Periodic 120 BPM kick should produce confident BPM", rhythmReport.rhythm.bpm)
        assertTrue(
            "Detected BPM (${rhythmReport.rhythm.bpm}) should be close to 120 BPM",
            rhythmReport.rhythm.bpm in 114..126
        )
        assertTrue("Rhythm confidence (${rhythmReport.rhythm.confidence}) must be >= 0.45", rhythmReport.rhythm.confidence >= 0.45f)
        assertTrue("Dynamic loud/quiet 8s mix must produce positive LRA (got ${rhythmReport.loudnessRangeLu})", rhythmReport.loudnessRangeLu >= 3.0f)

        // 2. Stationary pure 440Hz sine tone -> monochromatic Unknown/Mixed & null BPM
        val pureFrames = sr * 4
        val sineL = FloatArray(pureFrames) { i -> (0.3 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
        val sineR = FloatArray(pureFrames) { i -> (0.3 * sin(2.0 * PI * 440.0 * i / sr)).toFloat() }
        val sineReport = AudioAnalysisService.analyze(null, sineL, sineR, sr, 2, "calibration_tone.wav")

        assertTrue("Stationary calibration sine must fall back to Unknown / Mixed", sineReport.isUnknownOrMixedStyle)
        assertEquals(MusicTaxonomyRepository.UNKNOWN_MIXED_ID, sineReport.primaryTaxonomyId)
        assertNull("Stationary tone must NOT fabricate BPM", sineReport.rhythm.bpm)
    }

    @Test
    fun `contradictory filename cannot override acoustic classification or synthesize tempo`() {
        val sr = 48000
        val frames = sr * 6
        // Construct a soft, high-crest-factor, mid-centered orchestral-style sparse passage (~16-20 dB crest factor, <= -21 LUFS)
        // with NO regular dance/drill beat, while giving it a hyper-contradictory drill/trap/150bpm filename.
        val orchL = FloatArray(frames)
        val orchR = FloatArray(frames)
        for (i in 0 until frames) {
            val t = i.toDouble() / sr
            val swell = 0.04 + 0.05 * sin(2.0 * PI * 0.25 * t)
            // Rare slow expressive transient spikes so crest factor is high (~16-18 dB) while LUFS stays quiet (~ -24 LUFS)
            val bowTransient = if (i % 73000 in 0..45) 0.48 else 0.0
            orchL[i] = ((swell * (sin(2.0 * PI * 330.0 * t) + 0.5 * sin(2.0 * PI * 660.0 * t))) + bowTransient * sin(2.0 * PI * 520.0 * t)).toFloat()
            orchR[i] = ((swell * (sin(2.0 * PI * 330.0 * t + 0.45) + 0.5 * sin(2.0 * PI * 660.0 * t + 0.2))) + bowTransient * sin(2.0 * PI * 520.0 * t + 0.3)).toFloat()
        }

        val badFilename = "hard_drill_808_trap_mix_150bpm.wav"
        val report = AudioAnalysisService.analyze(null, orchL, orchR, sr, 2, badFilename)
        val trackInfo = AudioDecoder.analyzeBuffer(
            id = "contradictory_test",
            title = badFilename,
            sourceLabel = "LOCAL TEST",
            left = orchL,
            right = orchR,
            sampleRate = sr,
            channels = 2,
            isBundled = false
        )

        assertEquals(
            "Wide-dynamic quiet orchestral signal must map to classical_orchestral despite 'drill/trap' filename",
            "classical_orchestral",
            report.primaryTaxonomyId
        )
        assertEquals(
            "Legacy MusicGenre must also reject contradictory drill filename and detect CLASSICAL",
            MusicGenre.CLASSICAL,
            trackInfo.diagnostic.detectedGenre
        )
        assertNotNull(
            "Contradictory filename should emit transparent audit guard note without overriding scores",
            report.filenameHintNote
        )
        assertTrue(
            "Non-periodic orchestral signal must NOT fabricate '150' BPM from filename (got ${report.rhythm.bpm})",
            report.rhythm.bpm == null || report.rhythm.bpm != 150
        )
    }

    @Test
    fun `UnifiedStudioScreen renders Phase 2 multi-label styles, separated tags, spectral split, Unknown fallback, and findings`() {
        val sampleReport = AudioAnalysisReport(
            taxonomyVersion = 1,
            integratedLufs = -9.2f,
            loudnessRangeLu = 3.4f,
            truePeakDb = 0.2f,
            lowEnergyPct = 56,
            midEnergyPct = 30,
            highEnergyPct = 14,
            stereoCorrelation = 0.08f,
            hasPhaseRisk = true,
            isUnknownOrMixedStyle = true,
            primaryTaxonomyId = MusicTaxonomyRepository.UNKNOWN_MIXED_ID,
            primaryStyleLabel = "Unknown / Mixed",
            styleConfidence = 0.41f,
            multiLabelStyles = listOf(
                ScoredTaxonomyTag("unknown_mixed", "Unknown / Mixed", "Unclassified", 0.41f),
                ScoredTaxonomyTag("trap_808", "Trap / 808", "Hip-Hop", 0.39f)
            ),
            moodTags = listOf("High Energy / Intense"),
            productionStyleTags = listOf("Sub-Heavy Low End", "Side-Dominant / Phase Alert"),
            vocalActivity = VocalActivityEstimate(presencePct = 62, label = "VOCAL / SPEECH PROMINENT"),
            rhythm = RhythmEstimate(bpm = 138, confidence = 0.72f, feelLabel = "UPTEMPO / DRIVING"),
            technicalFindings = listOf(
                AudioTechnicalFinding(
                    code = "STEREO_PHASE_RISK",
                    title = "Mono Phase Cancellation Risk (r=+0.08)",
                    detail = "Low L/R phase correlation risks cancellation.",
                    severity = FindingSeverity.WARNING
                )
            ),
            filenameHintNote = "Filename hints 'Classical / Orchestral' but was NOT allowed to override measured acoustic features."
        )

        val state = WaviqUiState(
            trackInfo = AudioTrackInfo(
                diagnostic = TrackDiagnosticReport(analysisReport = sampleReport)
            )
        )

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedStudioScreen(
                    state = state,
                    onImportAudioClick = {},
                    onLoadReferenceClick = {},
                    onStartMicCaptureClick = {},
                    onStopMicCaptureClick = {},
                    onSeek = {},
                    onToggleAb = {},
                    onRunAutoMasterAi = {},
                    onUpdateAiInput = {},
                    onSubmitAiCommand = {},
                    onSetProSheetOpen = {},
                    onSelectProTuningTab = { _: ProTuningTab -> },
                    onUpdateBand = { _, _ -> },
                    onResetEq = {},
                    onUpdateDeck = {},
                    onSelectProfile = {},
                    onSelectGenre = {},
                    onUpdateStem = { _, _, _, _ -> },
                    onResetStems = {},
                    onToggleDspHook = { _, _ -> },
                    onSetOversampling = {},
                    onToggleHiResKhz = {},
                    onSelectGlyphPattern = {},
                    onApplyMasteringPreset = {},
                    onRerunStartupGuard = {},
                    onDismissStartupBanner = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("analysis_style_tags_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("UNKNOWN / MIXED").assertIsDisplayed()
        composeTestRule.onNodeWithTag("analysis_separated_tags_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("High Energy / Intense").assertIsDisplayed()
        composeTestRule.onNodeWithTag("analysis_spectral_balance_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("138 BPM").assertIsDisplayed()
        composeTestRule.onNodeWithTag("analysis_technical_findings").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Mono Phase Cancellation Risk (r=+0.08)").assertIsDisplayed()
    }
}
