package com.example.chessboard.ui.screen.trainSingleLine

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.example.chessboard.boardmodel.LineController
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.testing.fenStateDescriptionMatcher
import com.example.chessboard.ui.BoardOrientation
import com.example.chessboard.ui.ChessBoardWithCoordinates
import com.example.chessboard.ui.InteractiveChessBoardTestTag
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Rule
import org.junit.Test

class TrainSingleLineBoardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun trainingBoard_userMoveUpdatesVisibleBoard() {
        composeRule.setContent {
            ChessBoardTheme {
                TrainSingleLineBoardHarness(
                    orientation = BoardOrientation.WHITE,
                    uciMoves = listOf("e2e4", "e7e5")
                )
            }
        }

        composeRule.onNode(
            hasText("Start training") and hasClickAction()
        ).performClick()
        composeRule.waitForIdle()

        val boardNode = composeRule.onNodeWithTag(InteractiveChessBoardTestTag)
        boardNode.performTouchInput {
            val squareSize = width / 8f
            click(squareCenter(file = 4, row = 6, squareSize = squareSize))
        }
        boardNode.performTouchInput {
            val squareSize = width / 8f
            click(squareCenter(file = 4, row = 4, squareSize = squareSize))
        }

        composeRule.waitForIdle()
        boardNode.assert(
            fenStateDescriptionMatcher(
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
            )
        )
    }

    @Test
    fun trainingBoard_wrongMoveCountsMistakeAndResetsPosition() {
        composeRule.setContent {
            ChessBoardTheme {
                TrainSingleLineBoardHarness(
                    orientation = BoardOrientation.WHITE,
                    uciMoves = listOf("e2e4", "e7e5")
                )
            }
        }

        composeRule.onNode(
            hasText("Start training") and hasClickAction()
        ).performClick()
        composeRule.waitForIdle()

        val boardNode = composeRule.onNodeWithTag(InteractiveChessBoardTestTag)

        boardNode.performTouchInput {
            val squareSize = width / 8f
            click(squareCenter(file = 6, row = 7, squareSize = squareSize))
        }
        boardNode.performTouchInput {
            val squareSize = width / 8f
            click(squareCenter(file = 5, row = 5, squareSize = squareSize))
        }

        composeRule.waitForIdle()

        composeRule.onNode(hasText("Mistakes: 1")).assertExists()
        boardNode.assert(fenStateDescriptionMatcher(InitialBoardFen))
    }

    @Composable
    private fun TrainSingleLineBoardHarness(
        orientation: BoardOrientation,
        uciMoves: List<String>
    ) {
        var uiState by remember { mutableStateOf(TrainSingleLineUiState()) }
        val lineController = remember { LineController(orientation) }

        SideEffect {
            lineController.setUserMovesEnabled(resolveBoardInteractionEnabled(uiState))
            lineController.setAllowedMoveUci(null)
        }

        LaunchedEffect(uiState.phase, lineController.boardState, uiState.expectedPly, orientation, uciMoves) {
            uiState = handleTrainingProgress(
                uiState = uiState,
                lineController = lineController,
                uciMoves = uciMoves,
                currentOrientation = orientation,
                sidesCount = 1
            )
        }

        androidx.compose.foundation.layout.Column {
            androidx.compose.material3.TextButton(
                onClick = {
                    lineController.resetToStartPosition()
                    uiState = advanceProgramMoves(
                        uiState = buildStartTrainingState(uiState),
                        lineController = lineController,
                        uciMoves = uciMoves,
                        currentOrientation = orientation,
                        sidesCount = 1
                    )
                }
            ) {
                androidx.compose.material3.Text("Start training")
            }

            androidx.compose.material3.Text("Mistakes: ${uiState.mistakesCount}")

            ChessBoardWithCoordinates(
                lineController = lineController,
                modifier = Modifier.size(320.dp)
            )
        }
    }

    private fun squareCenter(file: Int, row: Int, squareSize: Float): Offset {
        return Offset(
            x = file * squareSize + squareSize / 2f,
            y = row * squareSize + squareSize / 2f
        )
    }

    private companion object {
        @Suppress("unused")
        val testLine = LineEntity(
            id = 1L,
            event = "Test Line",
            pgn = "1. e4 e5 *",
            initialFen = "",
            sideMask = SideMask.WHITE
        )
    }
}
