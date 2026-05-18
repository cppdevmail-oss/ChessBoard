package com.example.chessboard.ui.screen.training.create

/*
 * UI tests for statistics-based training creation.
 *
 * Keep screen-level selection, settings, and save-flow behavior tests here.
 * Do not add formula calculation tests, Room migration tests, or unrelated training editor tests here.
 *
 * Validation date: 2026-05-18
 */

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.runtimecontext.StatisticsTrainingRuntimeContext
import com.example.chessboard.service.uciMovesToMoves
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.theme.ChessBoardTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateTrainingByStatisticsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dbProvider: DatabaseProvider
        get() = DatabaseProvider.createInstance(composeRule.activity)

    @Before
    fun setUp() {
        dbProvider.clearAllData()
    }

    @Test
    fun createTrainingByStatisticsScreen_settingsChangeShowsSelectionOutOfDateMessage() {
        setCreateTrainingByStatisticsContent()

        waitForTextDisplayed("Max lines")
        composeRule.onNodeWithContentDescription("Increase Min days since last training").performClick()

        waitForTextDisplayed("Selection is out of date")
        composeRule.onNodeWithText(
            "Save will refresh the selected lines before creating the training."
        ).assertIsDisplayed()
    }

    @Test
    fun createTrainingByStatisticsScreen_outdatedSelectionSaveRefreshesBeforeTrainingCreation() {
        saveLine()
        setCreateTrainingByStatisticsContent()

        waitForTextDisplayed("Lines selected by statistics: 1")
        composeRule.onNodeWithContentDescription("Increase Min days since last training").performClick()
        waitForTextDisplayed("Selection is out of date")
        composeRule.onNodeWithContentDescription("Save").performClick()

        waitForTextDisplayed("Selection Refreshed")
        composeRule.onNodeWithText(
            "Lines were selected again from the current settings. You can now save the training."
        ).assertIsDisplayed()
        assertTextDoesNotExist("Training Created")
    }

    private fun setCreateTrainingByStatisticsContent() {
        composeRule.setContent {
            ChessBoardTheme {
                CreateTrainingByStatisticsScreenContainer(
                    screenContext = ScreenContainerContext(inDbProvider = dbProvider),
                    statisticsTrainingRuntimeContext = StatisticsTrainingRuntimeContext(),
                    onOpenFormulaSettings = {},
                )
            }
        }
    }

    private fun waitForTextDisplayed(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    private fun assertTextDoesNotExist(text: String) {
        composeRule.waitForIdle()
        check(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) {
            "Expected text '$text' to be absent"
        }
    }

    private fun saveLine(): Long {
        return runBlocking {
            val line = LineEntity(
                event = "Statistics Training Source",
                pgn = storedPgn(listOf("e2e4", "e7e5")),
                initialFen = "",
                sideMask = SideMask.BOTH,
            )

            val lineId = dbProvider.createLineSaver().saveLine(
                line = line,
                moves = uciMovesToMoves(listOf("e2e4", "e7e5")),
                sideMask = line.sideMask,
            )
            checkNotNull(lineId) {
                "Expected test line to be saved"
            }
        }
    }

    private fun storedPgn(moves: List<String>): String {
        return buildString {
            append("[Event \"Statistics Training Source\"]\n")
            append("[White \"White\"]\n")
            append("[Black \"Black\"]\n")
            append("[Result \"*\"]\n\n")

            moves.forEachIndexed { index, move ->
                if (index % 2 == 0) {
                    append("${index / 2 + 1}. ")
                }

                append(move)
                append(" ")
            }

            append("*")
        }
    }
}
