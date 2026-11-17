package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.TfliteDspHook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityStartupTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `MainActivity initializes WaviqApp and renders inspector controls without crashing`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("top_transport_play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dev_map_toggle_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_bus_latency_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("import_audio_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("load_ref_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("smart_diagnostic_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TfliteDspHook falls back gracefully when model asset is missing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hook = TfliteDspHook(context, modelAssetName = "non_existent_model.tflite")
        val left = FloatArray(512) { 0.01f }
        val right = FloatArray(512) { 0.01f }

        hook.processBlock(left, right, 512, 48000)

        assertEquals("DSP_FALLBACK", hook.runtimeMode)
        assertTrue(left.all { it.isFinite() })
        hook.close()
    }
}
