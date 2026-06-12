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
class EmptyStateUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testEmptyStatesAndNavigation() {
        // Complete Onboarding to get to an empty Home Screen
        composeTestRule.onNodeWithText("Welcome to RentSplit").assertExists()
        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithText("Name your Household").assertExists()
        composeTestRule.onNodeWithText("Household Name").performTextInput("Empty House")
        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithText("Who lives here?").assertExists()
        composeTestRule.onNodeWithText("Your Name").performTextInput("Kal")
        composeTestRule.onNodeWithText("Complete").performClick()

        // Wait for Home Screen
        composeTestRule.onNodeWithText("Total Expenses").assertExists()
        
        // Assert Home Screen Empty State
        composeTestRule.onNodeWithText("No expenses this month").assertExists()

        // Navigate to Balances Screen (Assume Bottom Nav has "Balances" label)
        composeTestRule.onNodeWithText("Balances").performClick()
        
        // Assert Balances Empty State
        composeTestRule.onNodeWithText("No outstanding balances").assertExists()

        // Navigate to History Screen (Assume Bottom Nav has "History" label)
        composeTestRule.onNodeWithText("History").performClick()
        
        // Assert History Empty State
        composeTestRule.onNodeWithText("No history yet").assertExists()
        
        // Test Add Expense validation failure (Empty fields)
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.onNodeWithContentDescription("Add Expense").performClick()
        
        composeTestRule.onNodeWithText("Save").performClick()
        // Wait for error text
        composeTestRule.onNodeWithText("Title cannot be empty").assertExists()
    }
}
