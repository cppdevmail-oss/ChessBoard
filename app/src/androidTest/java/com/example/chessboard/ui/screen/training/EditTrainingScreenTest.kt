package com.example.chessboard.ui.screen.training

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import com.example.chessboard.RuntimeContext
import com.example.chessboard.testing.fenStateDescriptionMatcher
import com.example.chessboard.ui.InteractiveChessBoardTestTag
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Rule
import org.junit.Test

class EditTrainingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editTrainingScreen_moveChipUpdatesVisibleBoardPosition() {
        composeRule.setContent {
            ChessBoardTheme {
                EditTrainingScreen(
                    trainingId = 1L,
                    gamesForTraining = listOf(TestTrainingGame),
                    orderGamesInTraining = RuntimeContext.OrderGamesInTraining()
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("move-chip-1.e4").performScrollTo()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("move-chip-1.e4")
            .performSemanticsAction(SemanticsActions.OnClick)

        assertBoardFen(AfterE4Fen)
    }

    @Test
    fun editTrainingScreen_nextArrowUpdatesVisibleBoardPosition() {
        composeRule.setContent {
            ChessBoardTheme {
                EditTrainingScreen(
                    trainingId = 1L,
                    gamesForTraining = listOf(TestTrainingGame),
                    orderGamesInTraining = RuntimeContext.OrderGamesInTraining()
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("move-legend-next").performScrollTo()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("move-legend-next").performClick()

        assertBoardFen(AfterE4Fen)
    }

    private fun assertBoardFen(expectedFen: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(InteractiveChessBoardTestTag).assert(
            fenStateDescriptionMatcher(expectedFen)
        )
    }

    private companion object {
        val TestTrainingGame = TrainingGameEditorItem(
            gameId = 1L,
            title = "Test Opening",
            pgn = "1. e2e4 e7e5 *"
        )
        const val AfterE4Fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
    }
}
