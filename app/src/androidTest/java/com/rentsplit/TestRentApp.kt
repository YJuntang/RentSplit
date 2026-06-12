package com.rentsplit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestRentApp {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppLaunchAndOnboarding() {
        // Wait for Welcome page to appear and click "Next"
        composeTestRule.onNodeWithText("Welcome to RentSplit").assertExists()
        composeTestRule.onNodeWithText("Next").performClick()

        // Wait for Household page to appear, enter household name and click "Next"
        composeTestRule.onNodeWithText("Name your Household").assertExists()
        composeTestRule.onNodeWithText("Household Name").performTextInput("Dream House")
        composeTestRule.onNodeWithText("Next").performClick()

        // Wait for Members page to appear, enter user's name and click "Complete"
        composeTestRule.onNodeWithText("Who lives here?").assertExists()
        composeTestRule.onNodeWithText("Your Name").performTextInput("Kal")
        composeTestRule.onNodeWithText("Complete").performClick()

        // Wait for home screen to load. 
        // The HomeScreen displays "Total Expenses" in HeroCard.
        composeTestRule.onNodeWithText("Total Expenses").assertExists()
        
        // Also verify the Add Expense floating action button is present using its content description
        composeTestRule.onNodeWithContentDescription("Add Expense").assertExists()
    }
}
