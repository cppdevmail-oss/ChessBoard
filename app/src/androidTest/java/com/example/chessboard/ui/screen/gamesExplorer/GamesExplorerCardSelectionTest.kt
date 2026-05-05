package com.example.chessboard.ui.screen.gamesExplorer

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.service.ParsedGame
import com.example.chessboard.service.buildMoveLabels
import com.example.chessboard.service.parsePgnMoves
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GamesExplorerCardSelectionTest {
    private val gameBlockTestTag = "game-block-initial-ply-selection"

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun gameBlock_clickingCardSelectsGameAtInitialPly() {
        val game = GameEntity(
            id = 7L,
            event = "Sicilian Defense",
            eco = "B20",
            pgn = "1. e2e4 c7c5 2. g1f3 *",
            initialFen = "",
            sideMask = SideMask.WHITE
        )
        val parsedGame = ParsedGame(
            game = game,
            uciMoves = parsePgnMoves(game.pgn),
            moveLabels = buildMoveLabels(parsePgnMoves(game.pgn))
        )
        var selectedPly = -1

        composeRule.setContent {
            ChessBoardTheme {
                GameBlock(
                    parsedGame = parsedGame,
                    isSelected = false,
                    currentPly = 0,
                    onSelectClick = { selectedPly = 0 },
                    canUndo = false,
                    canRedo = false,
                    onMovePlyClick = {},
                    onPrevClick = {},
                    onNextClick = {},
                    onResetClick = {},
                    onAnalyzeClick = {},
                    onCloneClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    modifier = Modifier.testTag(gameBlockTestTag),
                )
            }
        }

        composeRule.onNodeWithTag(gameBlockTestTag).performTouchInput {
            click(Offset(24f, 24f))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(0, selectedPly)
        }
    }
}
