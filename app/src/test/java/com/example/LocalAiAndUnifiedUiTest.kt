package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.audio.LocalAiMasteringAgent
import com.example.data.MasterTrackEntity
import com.example.model.AudioTrackInfo
import com.example.model.EnhanceProfile
import com.example.model.ExportFormat
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.ProTuningTab
import com.example.model.TactileDeckParams
import com.example.model.WaviqUiState
import com.example.model.defaultEqBands
import com.example.ui.screens.UnifiedLibraryScreen
import com.example.ui.screens.UnifiedStudioScreen
import com.example.ui.theme.WaviqTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalAiAndUnifiedUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `LocalAiMasteringAgent interprets Arabic and English sound commands accurately`() {
        val eq = defaultEqBands()
        val info = AudioTrackInfo()
        val deck = TactileDeckParams()

        // 1. Arabic bass boost intent
        val bassDecision = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "باص أقوى وعمق 808",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        assertTrue(bassDecision.eqBands[0].gainDb > eq[0].gainDb)
        assertTrue(bassDecision.deckParams.punch > deck.punch)
        assertTrue(bassDecision.appliedSummary.contains("Sub/Bass Boost"))

        // 2. Arabic Hi-Res 192kHz air + stereo widening intent
        val hiResWide = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "192kHz لمعان وتوسيع ستيريو",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.50f
        )
        assertEquals(192, hiResWide.targetKhz)
        assertEquals(OversamplingFactor.X8, hiResWide.oversampling)
        assertTrue(hiResWide.stereoExpansionAmount >= 0.8f)
        assertTrue(hiResWide.restorationAmount > 0.65f)

        // 3. Vocal clarity intent
        val vocal = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "وضوح الغناء ونقاء الصوت",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        assertEquals(EnhanceProfile.VOCAL_CLARITY, vocal.selectedProfile)
        assertTrue(vocal.eqBands[3].gainDb > eq[3].gainDb)

        // 4. Drill intent
        val drill = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "دريل باص 808 قوي وسناب حاد",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        assertEquals(MusicGenre.DRILL, drill.selectedGenre)
        assertEquals(OversamplingFactor.X8, drill.oversampling)
        assertTrue(drill.deckParams.punch >= 0.94f)
        assertTrue(drill.appliedSummary.contains("Drill"))

        // 5. Techno intent
        val techno = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "techno 4/4 industrial kick and rumble",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        assertEquals(MusicGenre.TECHNO, techno.selectedGenre)
        assertTrue(techno.deckParams.width >= 0.75f)
        assertTrue(techno.appliedSummary.contains("Techno"))

        // 6. Trap intent
        val trap = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "تراب 808 مشبع مع هاي-هاتس نقية",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        assertEquals(MusicGenre.TRAP, trap.selectedGenre)
        assertTrue(trap.deckParams.punch >= 0.88f)
        assertTrue(trap.appliedSummary.contains("Trap"))

        // 7. Unsupported command test: must NOT modify EQ or deck parameters
        val unsupported = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = "unsupported random gibberish 12345 xyz",
            info = info,
            currentEq = eq,
            currentDeck = deck,
            currentGenre = MusicGenre.POP,
            currentProfile = EnhanceProfile.STREAMING_MASTER,
            currentTargetKhz = 96,
            currentOversampling = OversamplingFactor.X4,
            currentRestoration = 0.55f
        )
        org.junit.Assert.assertFalse(unsupported.isSupported)
        assertEquals(0, unsupported.confidencePct)
        assertEquals(eq[0].gainDb, unsupported.eqBands[0].gainDb, 0.001f)
        assertEquals(deck.punch, unsupported.deckParams.punch, 0.001f)
        assertTrue(unsupported.appliedSummary.contains("UNSUPPORTED"))
    }

    @Test
    fun `UnifiedStudioScreen renders Local AI controls, dispatches prompt, and opens manual sheet`() {
        var autoMasterClicked = false
        var submittedPrompt = ""
        var openSheetRequested = false

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedStudioScreen(
                    state = WaviqUiState(aiCommandInput = "باص أقوى"),
                    onImportAudioClick = {},
                    onLoadReferenceClick = {},
                    onStartMicCaptureClick = {},
                    onStopMicCaptureClick = {},
                    onSeek = {},
                    onToggleAb = {},
                    onRunAutoMasterAi = { autoMasterClicked = true },
                    onUpdateAiInput = {},
                    onSubmitAiCommand = { submittedPrompt = it },
                    onSetProSheetOpen = { openSheetRequested = it },
                    onSelectProTuningTab = {},
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

        composeTestRule.onNodeWithTag("auto_master_ai_button").performScrollTo().performClick()
        assertTrue(autoMasterClicked)

        composeTestRule.onNodeWithTag("local_ai_apply_button").performScrollTo().performClick()
        assertEquals("باص أقوى", submittedPrompt)

        composeTestRule.onNodeWithTag("open_pro_sheet_button").performScrollTo().performClick()
        assertTrue(openSheetRequested)
    }

    @Test
    fun `UnifiedStudioScreen displays optional Pro Manual Tuning sheet with all 4 tabs`() {
        composeTestRule.setContent {
            WaviqTheme {
                UnifiedStudioScreen(
                    state = WaviqUiState(isProSheetOpen = true, proTuningTab = ProTuningTab.EQ),
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
                    onSelectProTuningTab = {},
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
        composeTestRule.onNodeWithTag("pro_manual_tuning_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_tab_eq").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_tab_deck").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_tab_stems").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_tab_engine").assertIsDisplayed()
    }

    @Test
    fun `UnifiedLibraryScreen exposes 192kHz export options and saved masters offline library`() {
        var chosenFormat: ExportFormat? = null
        val saved = listOf(
            MasterTrackEntity(
                id = 1,
                title = "NIGHT_DRIVE_MASTER.wav",
                formatLabel = "WAV 32-BIT FLOAT / 192kHz",
                sampleRate = 192000,
                bitDepth = 32,
                durationMs = 16000L,
                finalLufs = -14.0f,
                finalPeakDb = -1.0f,
                profileName = "STREAMING -14 LUFS",
                filePath = "/tmp/fake.wav"
            )
        )

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedLibraryScreen(
                    state = WaviqUiState(),
                    savedMasters = saved,
                    onUpdateConfig = { fmt, _, _ -> if (fmt != null) chosenFormat = fmt },
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

        composeTestRule.onNodeWithTag("export_format_wav_32f_192").performScrollTo().performClick()
        assertEquals(ExportFormat.WAV_32F_192, chosenFormat)

        composeTestRule.onNodeWithTag("saved_master_card_1").performScrollTo().assertIsDisplayed()
    }
}
