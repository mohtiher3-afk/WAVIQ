package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.StartupSubsystemCheck
import com.example.model.WaviqUiState
import com.example.ui.devmap.DeveloperMapScreen
import com.example.ui.screens.Screen01TrackInspector
import com.example.ui.theme.WaviqTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StartupGuardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `validate returns HEALTHY when TFLite model, permission, and Room DB pass`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { true },
            databasePing = { 3 }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.HEALTHY, report.healthState)
        assertFalse(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.permissionCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.databaseCheck.status)
        assertTrue(report.databaseCheck.detail.contains("3 saved masters"))
    }

    @Test
    fun `validate returns PERMISSION_ADVISORY when only RECORD_AUDIO is ungranted`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { false },
            databasePing = { 0 }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.PERMISSION_ADVISORY, report.healthState)
        assertFalse(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.ADVISORY, report.permissionCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.databaseCheck.status)
    }

    @Test
    fun `validate enters DEGRADED_FALLBACK when TFLite asset is missing or corrupt`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingGuard = StartupGuard(
            context = context,
            assetOpener = { throw FileNotFoundException("missing.tflite") },
            permissionGrantedProvider = { true },
            databasePing = { 1 }
        )
        val missingReport = missingGuard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, missingReport.healthState)
        assertTrue(missingReport.isDegraded)
        assertEquals(StartupCheckStatus.FALLBACK, missingReport.tfliteCheck.status)
        assertEquals(StartupCheckStatus.PASS, missingReport.databaseCheck.status)

        val corruptGuard = StartupGuard(
            context = context,
            assetOpener = { ByteArrayInputStream(ByteArray(32) { 0x01 }) },
            permissionGrantedProvider = { true },
            databasePing = { 1 }
        )
        val corruptReport = corruptGuard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, corruptReport.healthState)
        assertEquals(StartupCheckStatus.FALLBACK, corruptReport.tfliteCheck.status)
    }

    @Test
    fun `validate enters DEGRADED_FALLBACK when Room DB ping fails`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { true },
            databasePing = { throw IllegalStateException("SQLite locked") }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, report.healthState)
        assertTrue(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.FALLBACK, report.databaseCheck.status)
        assertTrue(report.fallbackSummary.contains("SQLite locked"))
    }

    @Test
    fun `Screen01 fallback card and Dev Map diagnostics display StartupGuard status`() {
        val degradedReport = StartupGuardReport(
            healthState = StartupHealthState.DEGRADED_FALLBACK,
            tfliteCheck = StartupSubsystemCheck("TFLITE MODEL", StartupCheckStatus.FALLBACK, "Using DSP gate"),
            permissionCheck = StartupSubsystemCheck("RECORD_AUDIO", StartupCheckStatus.ADVISORY, "Optional"),
            databaseCheck = StartupSubsystemCheck("ROOM SQLITE", StartupCheckStatus.PASS, "Connected (0 saved masters)"),
            fallbackSummary = "Degraded mode active: Using DSP gate",
            checkedAtMs = 1L
        )
        var rerunCount = 0
        var dismissCount = 0

        composeTestRule.setContent {
            WaviqTheme {
                Screen01TrackInspector(
                    state = WaviqUiState(startupGuard = degradedReport, isStartupBannerDismissed = false),
                    onImportAudioClick = {},
                    onLoadReferenceClick = {},
                    onStartMicCaptureClick = {},
                    onStopMicCaptureClick = {},
                    onSeek = {},
                    onGoToScope = {},
                    onRerunStartupGuard = { rerunCount++ },
                    onDismissStartupBanner = { dismissCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("startup_guard_fallback_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("startup_guard_rerun_button").performClick()
        composeTestRule.onNodeWithTag("startup_guard_dismiss_button").performClick()
        assertEquals(1, rerunCount)
        assertEquals(1, dismissCount)
    }

    @Test
    fun `DeveloperMapScreen displays StartupGuard diagnostics panel and triggers audit`() {
        var rerunCount = 0
        composeTestRule.setContent {
            WaviqTheme {
                DeveloperMapScreen(
                    state = WaviqUiState(
                        startupGuard = StartupGuardReport(
                            healthState = StartupHealthState.HEALTHY,
                            fallbackSummary = "All checks passed."
                        )
                    ),
                    onSelectFilter = {},
                    onInspectRoute = {},
                    onSimulateDispatch = { _, _ -> },
                    onRerunStartupGuard = { rerunCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("devmap_startup_guard_panel").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("devmap_startup_guard_rerun_button").performScrollTo().performClick()
        assertEquals(1, rerunCount)
    }
}
