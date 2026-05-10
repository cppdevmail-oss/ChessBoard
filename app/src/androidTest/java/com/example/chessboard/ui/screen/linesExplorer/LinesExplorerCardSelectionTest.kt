package com.example.chessboard.ui.screen.linesExplorer

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.example.chessboard.boardmodel.LineController
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.service.ParsedLine
import com.example.chessboard.service.buildMoveLabels
import com.example.chessboard.service.parsePgnMoves
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LinesExplorerCardSelectionTest {
    private val lineBlockTestTag = "line-block-initial-ply-selection"

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun lineBlock_clickingCardSelectsLineAtInitialPly() {
        val line = LineEntity(
            id = 7L,
            event = "Sicilian Defense",
            eco = "B20",
            pgn = "1. e2e4 c7c5 2. g1f3 *",
            initialFen = "",
            sideMask = SideMask.WHITE
        )
        val parsedLine = ParsedLine(
            line = line,
            uciMoves = parsePgnMoves(line.pgn),
            moveLabels = buildMoveLabels(parsePgnMoves(line.pgn))
        )
        var selectedPly = -1

        composeRule.setContent {
            ChessBoardTheme {
                LineBlock(
                    parsedLine = parsedLine,
                    isSelected = false,
                    lineController = LineController(),
                    onSelectClick = { selectedPly = 0 },
                    onMovePlyClick = {},
                    modifier = Modifier.testTag(lineBlockTestTag),
                )
            }
        }

        composeRule.onNodeWithTag(lineBlockTestTag).performTouchInput {
            click(Offset(24f, 24f))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(0, selectedPly)
        }
    }
}
