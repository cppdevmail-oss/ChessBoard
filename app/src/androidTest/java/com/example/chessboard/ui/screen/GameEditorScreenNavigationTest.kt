package com.example.chessboard.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.service.buildMoveLabels
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameEditorScreenNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun gameEditorScreen_rightArrowAdvancesBoardPosition() {
        val gameController = createGameControllerAtStart()
        val moveLabels = buildMoveLabels(TestUciMoves)

        composeRule.setContent {
            ChessBoardTheme {
                GameEditorScreen(
                    game = TestGame,
                    gameController = gameController,
                    moveLabels = moveLabels,
                    isLoading = false
                )
            }
        }

        composeRule.onNodeWithContentDescription("Next").performClick()

        composeRule.runOnIdle {
            assertEquals(1, gameController.currentMoveIndex)
            assertEquals(AfterE4Fen, gameController.getFen())
        }
    }

    @Test
    fun gameEditorScreen_moveChipAdvancesBoardPosition() {
        val gameController = createGameControllerAtStart()
        val moveLabels = buildMoveLabels(TestUciMoves)

        composeRule.setContent {
            ChessBoardTheme {
                GameEditorScreen(
                    game = TestGame,
                    gameController = gameController,
                    moveLabels = moveLabels,
                    isLoading = false
                )
            }
        }

        composeRule.onNodeWithText("1.e4").performClick()

        composeRule.runOnIdle {
            assertEquals(1, gameController.currentMoveIndex)
            assertEquals(AfterE4Fen, gameController.getFen())
        }
    }

    private fun createGameControllerAtStart(): GameController {
        return GameController().apply {
            loadFromUciMoves(TestUciMoves, targetPly = 0)
        }
    }

    private companion object {
        val TestUciMoves = listOf("e2e4", "e7e5")
        const val AfterE4Fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
        val TestGame = GameEntity(
            id = 1L,
            event = "Test Opening",
            pgn = "1. e4 e5 *",
            initialFen = "",
            sideMask = SideMask.BOTH
        )
    }
}
