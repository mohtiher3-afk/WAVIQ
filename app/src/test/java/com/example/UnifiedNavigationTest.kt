package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UnifiedNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `compact screen displays 2-tab bottom navigation bar and switches Studio to Library while keeping top utilities`() {
        composeTestRule.waitForIdle()

        // Top utilities preserved
        composeTestRule.onNodeWithTag("top_transport_play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_bus_latency_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dev_map_toggle_button").assertIsDisplayed()

        // Compact phone displays Bottom Navigation Bar and hides Left Rail
        composeTestRule.onNodeWithTag("waviq_bottom_nav_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("waviq_left_nav_rail").assertDoesNotExist()

        composeTestRule.onNodeWithTag("bottom_tab_studio").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag("bottom_tab_library").assertIsDisplayed().assertIsNotSelected()

        // Studio workspace content visible
        composeTestRule.onNodeWithTag("smart_diagnostic_card").performScrollTo().assertIsDisplayed()

        // Switch to Library tab
        composeTestRule.onNodeWithTag("bottom_tab_library").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bottom_tab_library").assertIsSelected()
        composeTestRule.onNodeWithTag("bottom_tab_studio").assertIsNotSelected()
        composeTestRule.onNodeWithTag("start_export_button").performScrollTo().assertIsDisplayed()

        // Switch back to Studio tab
        composeTestRule.onNodeWithTag("bottom_tab_studio").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bottom_tab_studio").assertIsSelected()
        composeTestRule.onNodeWithTag("auto_master_ai_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h800dp")
    fun `wide tablet screen adapts to Left Navigation Rail for 2-tab navigation`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("waviq_left_nav_rail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("waviq_bottom_nav_bar").assertDoesNotExist()

        composeTestRule.onNodeWithTag("rail_tab_studio").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag("rail_tab_library").assertIsDisplayed().assertIsNotSelected()

        // Switch to Library via Rail
        composeTestRule.onNodeWithTag("rail_tab_library").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("rail_tab_library").assertIsSelected()
        composeTestRule.onNodeWithTag("start_export_button").performScrollTo().assertIsDisplayed()

        // Switch back to Studio via Rail
        composeTestRule.onNodeWithTag("rail_tab_studio").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("rail_tab_studio").assertIsSelected()
        composeTestRule.onNodeWithTag("smart_diagnostic_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `switching Studio to Library and back preserves Studio scroll offset`() {
        composeTestRule.waitForIdle()

        // Scroll down to bottom button in Studio
        composeTestRule.onNodeWithTag("open_pro_sheet_button").performScrollTo().assertIsDisplayed()

        // Switch to Library
        composeTestRule.onNodeWithTag("bottom_tab_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("start_export_button").performScrollTo().assertIsDisplayed()

        // Return to Studio — bottom button should already be scrolled near/in view without jumping to top
        composeTestRule.onNodeWithTag("bottom_tab_studio").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("open_pro_sheet_button").assertIsDisplayed()
    }
}
