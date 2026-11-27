package com.example.financeapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AsyncTransactionsPracticeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun exercise9() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.async_load)).performClick()
        val operation = "Кофе: −250 ₽"
        waitForText(operation)
        composeTestRule.onNodeWithText(operation).assertIsDisplayed()
    }

    @Test
    fun exercise10() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.async_simulate_error)).performClick()
        val error = composeTestRule.activity.getString(R.string.async_error)
        waitForText(error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
        val retry = composeTestRule.activity.getString(R.string.async_retry)
        composeTestRule.onNodeWithText(retry).performClick()
        val operation = "Кофе: −250 ₽"
        waitForText(operation)
        composeTestRule.onNodeWithText(operation).assertIsDisplayed()
    }
    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
