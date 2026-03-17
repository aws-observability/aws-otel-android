package com.example.petclinic

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserInteractionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testGenerateComprehensiveTelemetry() {
        composeTestRule.waitForIdle()
        generateAllTelemetry()
        Thread.sleep(300)
    }

    @Test
    fun testGenerateCrashTelemetry() {
        composeTestRule.waitForIdle()
        scrollToBottom()
        triggerCrash()
        // App will crash and relaunch to capture crash telemetry
    }

    private fun generateAllTelemetry() {
        navigateToOwnersScreen()
        navigateToVetsScreen()
        navigateToHomeScreen()
        testUIJankFeature()
        performFinalNavigationRound()
        performDestructiveTests()
    }

    private fun navigateToOwnersScreen() {
        composeTestRule.onNodeWithText("Owners").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(300)
    }

    private fun navigateToVetsScreen() {
        composeTestRule.onNodeWithText("Veterinarians").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(300)
    }

    private fun navigateToHomeScreen() {
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(300)
    }

    private fun testUIJankFeature() {
        scrollToBottom()
        try {
            composeTestRule.onNodeWithText("🐌 Start UI Jank").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.waitUntil(3000) {
                composeTestRule.onAllNodesWithText("✅ Stop UI Jank").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("✅ Stop UI Jank").performClick()
            composeTestRule.waitForIdle()
        } catch (e: Exception) {
            // Continue if buttons not found
        }
    }

    private fun performFinalNavigationRound() {
        navigateToOwnersScreen()
        navigateToVetsScreen()
        navigateToHomeScreen()
    }

    private fun performDestructiveTests() {
        scrollToBottom()

        // Test API Call
        tryClick("🌐 Test API Call")

        // Network Error 404
        tryClick("🚫 Simulate Network Error 404")
        dismissAlert()

        // Network Error 500
        tryClick("🚫 Simulate Network Error 500")
        dismissAlert()

        // Trigger ANR
        tryClick("⏰ Trigger ANR")
    }

    private fun triggerCrash() {
        tryClick("💥 Trigger App Crash")
    }

    private fun tryClick(text: String) {
        try {
            composeTestRule.onNodeWithText(text).performClick()
            Thread.sleep(1000)
        } catch (e: Exception) {
            // Continue if button not found
        }
    }

    private fun dismissAlert() {
        try {
            composeTestRule.onNodeWithText("OK").performClick()
            Thread.sleep(500)
        } catch (e: Exception) {
            // Continue if alert not shown
        }
    }

    private fun scrollToBottom() {
        composeTestRule.onRoot().performTouchInput {
            swipeUp(startY = centerY + 300, endY = centerY - 300, durationMillis = 800)
        }
        Thread.sleep(500)
    }
}
