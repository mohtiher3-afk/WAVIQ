package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.ClassificationSource
import com.example.audio.LocalAiMasteringAgent
import com.example.audio.OnDeviceModelRegistry
import com.example.model.AudioAnalysisReport
import com.example.model.AudioTrackInfo
import com.example.model.DspDecisionStage
import com.example.model.EnhanceProfile
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.ProTuningTab
import com.example.model.TactileDeckParams
import com.example.model.TrackDiagnosticReport
import com.example.model.WaviqUiState
import com.example.model.defaultEqBands
import com.example.ui.screens.UnifiedStudioScreen
import com.example.ui.theme.WaviqTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase3IntelligenceAndSafetyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `bilingual parser handles negation cleanly without accidentally boosting negated bands`() {
        val eq = defaultEqBands()
        val info = AudioTrackInfo()
        val deck = TactileDeckParams(drive = 0.45f, width = 0.65f, punch = 0.70f, air = 0.55f, ceilingDb = -0.8f)

        // English negation: "no bass boost, don't widen, less bright"
        val enNeg = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "no bass boost, don't widen, less bright",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.60f
        )
        assertTrue(enNeg.isSupported)
        assertTrue("Expected SUB band reduced on 'no bass boost', got ${enNeg.eqBands[0].gainDb}", enNeg.eqBands[0].gainDb < eq[0].gainDb)
        assertTrue("Expected AIR band reduced on 'less bright', got ${enNeg.eqBands[4].gainDb}", enNeg.eqBands[4].gainDb < eq[4].gainDb)
        assertTrue("Expected width reduced on 'don't widen', got ${enNeg.deckParams.width}", enNeg.deckParams.width < deck.width)

        // Arabic negation: "بدون باص وقلل الحدة وحافظ على الديناميكية"
        val arNeg = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "بدون باص وقلل الحدة وحافظ على الديناميكية",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.60f
        )
        assertTrue(arNeg.isSupported)
        assertTrue("Expected SUB cut on 'بدون باص'", arNeg.eqBands[0].gainDb < eq[0].gainDb)
        assertTrue("Expected AIR cut on 'قلل الحدة'", arNeg.eqBands[4].gainDb < eq[4].gainDb)
        assertTrue("Expected drive capped on 'حافظ على الديناميكية'", arNeg.deckParams.drive <= 0.18f)
        assertTrue("Expected safe ceiling on dynamic preservation", arNeg.deckParams.ceilingDb <= -1.2f)
    }

    @Test
    fun `contradictory commands and unsupported stem-separation requests leave DSP parameters untouched`() {
        val eq = defaultEqBands()
        val info = AudioTrackInfo()
        val deck = TactileDeckParams(drive = 0.32f, width = 0.58f, punch = 0.55f, air = 0.45f, ceilingDb = -1.0f)

        val contradictions = listOf(
            "more bass and less bass",
            "wide stereo and mono",
            "باص أقوى وبدون باص",
            "club loud and preserve dynamics"
        )
        contradictions.forEach { prompt ->
            val res = LocalAiMasteringAgent.applyIntentCommand(
                rawCommand = prompt,
                info = info,
                currentEq = eq,
                currentDeck = deck,
                currentGenre = MusicGenre.POP,
                currentProfile = EnhanceProfile.STREAMING_MASTER,
                currentTargetKhz = 96,
                currentOversampling = OversamplingFactor.X4,
                currentRestoration = 0.55f
            )
            assertFalse("Contradictory prompt '$prompt' must be rejected", res.isSupported)
            assertEquals(0, res.confidencePct)
            assertEquals(eq, res.eqBands)
            assertEquals(deck, res.deckParams)
            assertTrue(res.appliedSummary.contains("CONTRADICTORY"))
        }

        // Unsupported stem separation prompts
        listOf("separate vocals and drums stems", "عزل الصوت وفصل الآلات").forEach { prompt ->
            val stemReq = LocalAiMasteringAgent.applyIntentCommand(
                rawCommand = prompt,
                info = info,
                currentEq = eq,
                currentDeck = deck,
                currentGenre = MusicGenre.POP,
                currentProfile = EnhanceProfile.STREAMING_MASTER,
                currentTargetKhz = 96,
                currentOversampling = OversamplingFactor.X4,
                currentRestoration = 0.55f
            )
            assertFalse("Stem separation request '$prompt' must be rejected honestly", stemReq.isSupported)
            assertEquals(eq, stemReq.eqBands)
            assertEquals(deck, stemReq.deckParams)
            assertTrue(stemReq.unsupportedGuidance?.contains("Band Mixer") == true)
        }
    }

    @Test
    fun `safety engine clamps blind boosts when measured audio shows phase risk, over-compression, hot peak, or sub saturation`() {
        val eq = defaultEqBands()
        val deck = TactileDeckParams()

        // 1. Phase-risk track resists widening command
        val phaseRiskInfo = AudioTrackInfo(
            diagnostic = TrackDiagnosticReport(
                analysisReport = AudioAnalysisReport(
                    stereoCorrelation = 0.05f,
                    hasPhaseRisk = true,
                    lowEnergyPct = 32,
                    midEnergyPct = 46,
                    highEnergyPct = 22
                )
            )
        )
        val wideOnBadPhase = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "توسيع ستيريو مسرح سينمائي",
            info = phaseRiskInfo,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.ELECTRONIC,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.50f
        )
        assertTrue(wideOnBadPhase.isSupported)
        assertTrue("Width must be clamped <= 0.42 on phase risk (was ${wideOnBadPhase.deckParams.width})", wideOnBadPhase.deckParams.width <= 0.42f)
        assertTrue("Stereo expansion must be clamped <= 0.25", wideOnBadPhase.stereoExpansionAmount <= 0.25f)
        assertTrue(wideOnBadPhase.safetyWarnings.any { it.contains("Stereo safety clamp") })
        assertTrue(wideOnBadPhase.stages.any { it.safetyClamped && it.stageName.contains("Stereo") })

        // 2. Over-compressed + hot-peaked + sub-heavy track resists blind Trap/Club slam
        val crushedHotSubInfo = AudioTrackInfo(
            integratedLufs = -8.4f,
            truePeakDb = 0.4f,
            dynamicRangeDb = 4.8f,
            diagnostic = TrackDiagnosticReport(
                analysisReport = AudioAnalysisReport(
                    integratedLufs = -8.4f,
                    truePeakDb = 0.4f,
                    crestFactorDb = 4.8f,
                    lowEnergyPct = 58,
                    midEnergyPct = 28,
                    highEnergyPct = 14
                )
            )
        )
        val trapOnCrushed = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "club loud trap 808",
            info = crushedHotSubInfo,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.50f
        )
        assertTrue(trapOnCrushed.isSupported)
        assertTrue("60 Hz sub boost must be clamped <= 1.5 dB when source low band is 58%", trapOnCrushed.eqBands[0].gainDb <= 1.5f)
        assertTrue("Drive must be capped <= 0.28 on compressed track", trapOnCrushed.deckParams.drive <= 0.28f)
        assertTrue("Ceiling must be locked <= -1.0 dBTP on hot peak", trapOnCrushed.deckParams.ceilingDb <= -1.0f)
        assertEquals(OversamplingFactor.X8, trapOnCrushed.oversampling)
        assertTrue(trapOnCrushed.safetyWarnings.size >= 2)
        assertTrue(trapOnCrushed.stages.size >= 4)
        assertTrue(trapOnCrushed.measurementsUsed.isNotEmpty())
    }

    @Test
    fun `OnDeviceModelRegistry reports genuine model provenance and UnifiedStudioScreen renders Phase 3 explainability`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = OnDeviceModelRegistry.detectStyleClassificationSource(context)
        assertEquals(ClassificationSource.HEURISTIC_ACOUSTIC, source)

        val models = OnDeviceModelRegistry.getRegisteredModels(context)
        assertTrue(models.any { it.assetName == "ai_audio_mastering_model.tflite" && it.isBundledAndVerified })
        val genreSpec = models.first { it.id == "genre_style_classifier" }
        assertFalse(genreSpec.isBundledAndVerified)

        val state = WaviqUiState(
            classificationSourceLabel = source.badgeLabel,
            decisionTargetStandard = "Streaming Delivery (-14.0 LUFS / -1.0 dBTP)",
            decisionSafetyWarnings = listOf(
                "Stereo safety clamp: Restricted M/S width (42%) because phase correlation is r=+0.05."
            ),
            decisionStages = listOf(
                DspDecisionStage(
                    stageName = "Corrective 5-Band EQ",
                    parameterSummary = "SUB +1.5dB • LOW -0.8dB",
                    reason = "Measured L/M/H split (48%/36%/16%)",
                    safetyClamped = true
                ),
                DspDecisionStage(
                    stageName = "3-Band True-Peak Limiter",
                    parameterSummary = "-1.0 dBTP • 8X ISP",
                    reason = "Source True Peak +0.2 dBTP",
                    safetyClamped = false
                )
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
        composeTestRule.onNodeWithTag("classification_source_badge").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("CLASSIFIER: HEURISTIC ACOUSTIC").assertIsDisplayed()
        composeTestRule.onNodeWithTag("decision_safety_warnings").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("decision_explainability_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("SAFE CLAMP").assertIsDisplayed()
    }
}
