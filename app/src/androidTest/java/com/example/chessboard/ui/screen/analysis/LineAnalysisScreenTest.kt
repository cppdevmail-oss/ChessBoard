package com.example.chessboard.ui.screen.analysis

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.testing.fenStateDescriptionMatcher
import com.example.chessboard.testing.normalizeFenForAssertion
import com.example.chessboard.ui.LineAnalysisContentTestTag
import com.example.chessboard.ui.LineAnalysisMoveControlsTestTag
import com.example.chessboard.ui.LineAnalysisSearchActionTestTag
import com.example.chessboard.ui.InteractiveChessBoardTestTag
import com.example.chessboard.ui.MoveTreeBoxTestTag
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LineAnalysisScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun lineAnalysisScreen_rendersStandardContent() {
        setAnalysisContent(onSearchByPositionClick = {})

        composeRule.onNodeWithText("Analyze Line").assertIsDisplayed()
        composeRule.onNodeWithTag(LineAnalysisContentTestTag).assertIsDisplayed()
        composeRule.onNodeWithTag(LineAnalysisMoveControlsTestTag).assertIsDisplayed()
        composeRule.onNodeWithText("White").assertIsDisplayed()
        composeRule.onNodeWithText("Black").assertIsDisplayed()
        composeRule.onNodeWithTag(InteractiveChessBoardTestTag).assert(
            fenStateDescriptionMatcher(InitialBoardFen)
        )
        scrollToTag(MoveTreeBoxTestTag)
        composeRule.onNodeWithTag(MoveTreeBoxTestTag).assertIsDisplayed()
    }

    @Test
    fun lineAnalysisScreen_loadsFromFenInitialPosition() {
        setAnalysisContent(
            initialPosition = LineAnalysisInitialPosition.FromFen("4k3/8/8/8/8/8/8/4K3 b - -"),
            onSearchByPositionClick = {},
        )

        assertBoardFenEventually("4k3/8/8/8/8/8/8/4K3 b - - 0 1")
    }

    @Test
    fun lineAnalysisScreen_searchActionReturnsCurrentFen() {
        var searchedFen = ""

        setAnalysisContent(
            initialPosition = LineAnalysisInitialPosition.FromFen("4k3/8/8/8/8/8/8/4K3 b - -"),
            onSearchByPositionClick = { searchedFen = it },
        )

        composeRule.onNodeWithTag(LineAnalysisSearchActionTestTag).performClick()

        composeRule.runOnIdle {
            assertEquals("4k3/8/8/8/8/8/8/4K3 b - - 0 1", searchedFen)
        }
    }

    private fun setAnalysisContent(
        initialPosition: LineAnalysisInitialPosition = LineAnalysisInitialPosition.StartPosition,
        onSearchByPositionClick: (String) -> Unit,
    ) {
        val dbProvider = DatabaseProvider.createInstance(composeRule.activity)
        composeRule.setContent {
            ChessBoardTheme {
                LineAnalysisScreenContainer(
                    screenContext = ScreenContainerContext(inDbProvider = dbProvider),
                    initialPosition = initialPosition,
                    onSearchByPositionClick = onSearchByPositionClick,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun scrollToTag(tag: String) {
        composeRule.onNodeWithTag(LineAnalysisContentTestTag)
            .performScrollToNode(hasTestTag(tag))
        composeRule.waitForIdle()
    }

    private fun assertBoardFenEventually(expectedFen: String) {
        val normalizedExpectedFen = normalizeFenForAssertion(expectedFen)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            currentBoardFen()?.let(::normalizeFenForAssertion) == normalizedExpectedFen
        }
    }

    private fun currentBoardFen(): String? {
        return runCatching {
            composeRule.onNodeWithTag(InteractiveChessBoardTestTag)
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.StateDescription)
        }.getOrNull()
    }
}
