package com.adit.sirs

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SirsIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun loginFlow_movesToDashboard() {
        // Find Email & Password inputs on the Login screen and enter test credentials
        composeTestRule.onNodeWithText("Email").performTextInput("budi@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("Budi12345!")

        // Wait a small moment to ensure the Compose State updates
        Thread.sleep(500)

        // Click the Login button
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for network login to finish and navigate to the User Dashboard (wait up to 30 seconds for slow emulator networking)
        composeTestRule.waitUntilExactlyOneExists(hasText("Dashboard"), timeoutMillis = 30000)

        // Verify Dashboard Elements are visible
        composeTestRule.onNodeWithText("Recent Reports").assertExists()
    }
}
