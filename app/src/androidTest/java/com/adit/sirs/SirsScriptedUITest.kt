package com.adit.sirs

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SirsScriptedUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun executeAllFeatures() {
        // 1. Login
        composeTestRule.onNodeWithText("Email").performTextInput("budi@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("Budi12345!")
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for dashboard to load
        composeTestRule.waitUntilExactlyOneExists(hasText("Dashboard"), timeoutMillis = 30000)

        // 2. Click Create Report (The floating action button doesn't have text, but it has contentDescription)
        composeTestRule.onNode(androidx.compose.ui.test.hasContentDescription("Create Report")).performClick()
        
        // Wait for Create Report screen
        composeTestRule.waitUntilExactlyOneExists(hasText("Create Report"), timeoutMillis = 10000)
        
        // 3. Fill the Report
        composeTestRule.onNodeWithText("Title *").performTextInput("Test Automation Report")
        composeTestRule.onNodeWithText("Description *").performTextInput("This is an automated test report checking if validation, connection, and data input perfectly matches PRD specifications.")
        composeTestRule.onNodeWithText("Location *").performTextInput("Server Room 1")
        
        // 4. We skip dropdown and date pickers for now as they require complex semantics interactions.
        // We will just hit Submit to see validation kick in (since category and date are empty).
        composeTestRule.onNodeWithText("Submit Report").performClick()
        
        // Wait for validation error to appear
        composeTestRule.waitUntilExactlyOneExists(hasText("Please fill all required fields correctly"), timeoutMillis = 10000)
    }
}
