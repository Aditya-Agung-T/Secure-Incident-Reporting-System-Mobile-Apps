package com.adit.sirs

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SirsAppUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_showsCorrectElements() {
        // Verify Title and Subtitle
        composeTestRule.onNodeWithText("SIRS").assertExists()
        composeTestRule.onNodeWithText("Secure Incident Reporting System").assertExists()

        // Verify Inputs and Buttons
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertExists()
    }

    @Test
    fun registerScreen_navigationAndValidation() {
        // Navigate to register
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        
        // Wait for screen to change and assert Registration elements
        composeTestRule.onNodeWithText("Full Name").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Confirm Password").assertExists()

        // Fill data
        composeTestRule.onNodeWithText("Full Name").performTextInput("User Test")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("Password123!")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("Password123!")

        // Register button should be visible
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Register")).fetchSemanticsNodes().isNotEmpty()
    }
}
