package com.example

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.model.AudioAnalysisReport
import com.example.model.AudioTrackInfo
import com.example.model.ExportJobState
import com.example.model.ProTuningTab
import com.example.model.ProcessingStatus
import com.example.model.TrackDiagnosticReport
import com.example.model.WaviqUiState
import com.example.ui.screens.UnifiedLibraryScreen
import com.example.ui.screens.UnifiedStudioScreen
import com.example.ui.theme.WaviqTheme
import com.example.viewmodel.WaviqViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase4UiPolishAndLifecycleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `UnifiedStudioScreen displays empty, analyzing, dismissible error, and processing cancel states`() {
        var cancelProcessingClicked = false
        var clearErrorClicked = false

        val emptyLoadingErrorRunningState = WaviqUiState(
            trackInfo = AudioTrackInfo(
                title = "EMPTY_TAKE.wav",
                durationMs = 0L,
                waveformPeaks = emptyList()
            ),
            isLoadingFile = true,
            fileErrorMessage = "Corrupt PCM header detected.",
            processing = ProcessingStatus(
                isRunning = true,
                progress = 0.45f,
                currentStage = "CALIBRATING 64-BIT BUS..."
            )
        )

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedStudioScreen(
                    state = emptyLoadingErrorRunningState,
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
                    onDismissStartupBanner = {},
                    onCancelProcessing = { cancelProcessingClicked = true },
                    onClearFileError = { clearErrorClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("studio_empty_state_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studio_analyzing_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studio_file_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dismiss_file_error_button").performClick()
        assertTrue("Expected error dismiss callback", clearErrorClicked)

        composeTestRule.onNodeWithTag("cancel_processing_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancel_processing_button").performClick()
        assertTrue("Expected processing cancel callback", cancelProcessingClicked)
    }

    @Test
    fun `UnifiedStudioScreen displays cancelled calibration banner and Pro Diagnostics tab`() {
        val diagState = WaviqUiState(
            processing = ProcessingStatus(
                isRunning = false,
                isCancelled = true,
                currentStage = "CANCELLED BY USER"
            ),
            isProSheetOpen = true,
            proTuningTab = ProTuningTab.DIAGNOSTICS,
            trackInfo = AudioTrackInfo(
                diagnostic = TrackDiagnosticReport(
                    analysisReport = AudioAnalysisReport(
                        integratedLufs = -14.2f,
                        momentaryMaxLufs = -10.8f,
                        shortTermMaxLufs = -12.1f,
                        loudnessRangeLu = 6.4f,
                        samplePeakDb = -1.6f,
                        truePeakDb = -1.2f
                    )
                )
            )
        )

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedStudioScreen(
                    state = diagState,
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
        composeTestRule.onNodeWithTag("studio_processing_status_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_manual_tuning_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_tab_diagnostics").assertIsDisplayed()
        composeTestRule.onNodeWithText("BS.1770 LOUDNESS & PEAK MATRIX").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_diagnostics_model_registry").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Block Dynamic Gate & Gain MLP").assertIsDisplayed()
    }

    @Test
    fun `UnifiedLibraryScreen renders exporting cancel, error, completed, and empty states`() {
        var cancelExportClicked = false
        var backToStudioClicked = false

        composeTestRule.setContent {
            WaviqTheme {
                UnifiedLibraryScreen(
                    state = WaviqUiState(
                        exportJob = ExportJobState(
                            isExporting = true,
                            progress = 0.52f,
                            stageLabel = "ENCODING WAV FRAMES"
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
                    onDeleteSavedMaster = {},
                    onCancelExport = { cancelExportClicked = true },
                    onGoToStudioClick = { backToStudioClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cancel_export_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancel_export_button").performClick()
        assertTrue("Expected cancel export callback", cancelExportClicked)

        composeTestRule.onNodeWithTag("library_empty_state").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_empty_back_to_studio_button").performScrollTo().performClick()
        assertTrue("Expected back to studio CTA callback", backToStudioClicked)
    }

    @Test
    fun `UnifiedLibraryScreen shows cancelled, error, and completed export surfaces accurately`() {
        composeTestRule.setContent {
            WaviqTheme {
                UnifiedLibraryScreen(
                    state = WaviqUiState(
                        exportJob = ExportJobState(
                            isExporting = false,
                            isCancelled = true,
                            errorMessage = "Insufficient local disk space.",
                            lastExportedPath = "/tmp/master_test.wav",
                            lastExportedTitle = "MASTER_TEST.wav",
                            lastExportedFormat = "WAV 24-BIT / 48kHz",
                            progress = 1f,
                            bytesWritten = 65536L
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
        composeTestRule.onNodeWithTag("library_export_cancelled_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_export_error_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("library_export_completed_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `WaviqViewModel cancelProcessing and cancelExportMaster update lifecycle states cleanly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = WaviqViewModel(app)

        vm.runAutoMasterAi()
        vm.cancelProcessing()
        val afterCancelProc = vm.uiState.value
        assertFalse(afterCancelProc.processing.isRunning)
        assertTrue(afterCancelProc.processing.isCancelled)

        vm.startExportMaster(onReadyForModal = {}, onCompleteDone = {})
        vm.cancelExportMaster()
        val afterCancelExport = vm.uiState.value
        assertFalse(afterCancelExport.exportJob.isExporting)
        assertTrue(afterCancelExport.exportJob.isCancelled)
        assertNull(afterCancelExport.exportJob.errorMessage)

        vm.clearFileError()
        assertNull(vm.uiState.value.fileErrorMessage)
    }

    @Test
    fun `UnifiedStudioScreen renders mixed Arabic and English content cleanly in RTL LayoutDirection`() {
        val rtlState = WaviqUiState(
            aiCommandInput = "وضوح الغناء بدون تشويه",
            aiAppliedSummary = "Applied Arabic vocal clarity profile with 8X ISP safety guard"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WaviqTheme {
                    UnifiedStudioScreen(
                        state = rtlState,
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
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("smart_diagnostic_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("analysis_separated_tags_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_pro_sheet_button").performScrollTo().assertIsDisplayed()
    }
}
