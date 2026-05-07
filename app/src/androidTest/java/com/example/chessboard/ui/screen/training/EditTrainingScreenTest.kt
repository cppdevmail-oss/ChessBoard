package com.example.chessboard.ui.screen.training
import com.example.chessboard.ui.screen.training.train.EditTrainingScreen
import com.example.chessboard.ui.screen.training.common.TrainingGameEditorItem

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.entity.SideMask
import com.example.chessboard.testing.fenStateDescriptionMatcher
import com.example.chessboard.testing.normalizeFenForAssertion
import com.example.chessboard.ui.EditTrainingListTestTag
import com.example.chessboard.ui.EditTrainingMoveLegendSectionTestTag
import com.example.chessboard.ui.InteractiveChessBoardTestTag
import com.example.chessboard.ui.moveChipTestTag
import com.example.chessboard.ui.theme.ChessBoardTheme
import org.junit.Rule
import org.junit.Test

class EditTrainingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editTrainingScreen_moveChipUpdatesVisibleBoardPosition() {
        setEditTrainingScreenContent(gamesForTraining = listOf(TestTrainingGame))

        composeRule.waitForIdle()
        // This screen auto-scrolls to the selected game with animateScrollToItem(...).
        // On slower emulators that animation can overlap with performScrollTo(), so we wait
        // until the target node is actually displayed before clicking it.
        composeRule.onNodeWithTag(EditTrainingListTestTag)
            .performScrollToNode(hasTestTag(EditTrainingMoveLegendSectionTestTag))
        waitForNodeDisplayed(EditTrainingMoveLegendSectionTestTag)
        composeRule.onNodeWithTag(moveChipTestTag("e4")).performScrollTo()
        waitForNodeDisplayed(moveChipTestTag("e4"))
        composeRule.onNodeWithTag(moveChipTestTag("e4"))
            .performSemanticsAction(SemanticsActions.OnClick)

        assertBoardFenEventually(AfterE4Fen)
    }

    @Test
    fun editTrainingScreen_nextArrowUpdatesVisibleBoardPosition() {
        setEditTrainingScreenContent(gamesForTraining = listOf(TestTrainingGame))

        composeRule.waitForIdle()
        // Keep these waits. The test is intentionally defensive because this screen has
        // multiple async pieces of UI work:
        // 1. LazyColumn.performScrollTo() moves the list to the legend controls.
        // 2. The screen itself also auto-scrolls to the selected game via animateScrollToItem(...).
        // 3. The selected game's moves are loaded in LaunchedEffect(...), and only after that
        //    canRedo becomes true and the next-arrow click reliably advances the board.
        // Removing these waits tends to make the test flaky on slower emulators.
        composeRule.onNodeWithTag(EditTrainingListTestTag)
            .performScrollToNode(hasTestTag(EditTrainingMoveLegendSectionTestTag))
        waitForNodeDisplayed(EditTrainingMoveLegendSectionTestTag)
        waitForNodeDisplayedByContentDescription("Next move")
        // Wait for the board to settle at the initial position before clicking next.
        // This makes the test assert the intended transition: start position -> first move.
        assertBoardFenEventually(InitialBoardFen)
        composeRule.onNodeWithContentDescription("Next move").performClick()

        assertBoardFenEventually(AfterE4Fen)
    }

    @Test
    fun editTrainingScreen_startTrainingActionLaunchesTraining() {
        var launchedGameId: Long? = null

        setEditTrainingScreenContent(
            gamesForTraining = listOf(TestTrainingGame),
            onStartGameTrainingClick = { gameId, _ ->
                launchedGameId = gameId
            },
        )

        waitForNodeDisplayedByContentDescription("Start training")
        composeRule.onNodeWithContentDescription("Start training").performClick()

        composeRule.runOnIdle {
            assert(launchedGameId == TestTrainingGame.gameId)
        }
    }

    @Test
    fun editTrainingScreen_removeSelectedGameRemovesItFromTraining() {
        setEditTrainingScreenContent(
            gamesForTraining = listOf(
                TestTrainingGame,
                SecondTrainingGame,
            ),
        )

        composeRule.onNodeWithContentDescription("Remove game from training").performClick()
        waitForTextDisplayed("Remove Game")
        composeRule.onNode(hasText("Remove") and hasClickAction()).performClick()

        waitForTextInTree("Games in training: 1")
        composeRule.onNodeWithTag(EditTrainingListTestTag)
            .performScrollToNode(hasText("Games in training: 1"))
        waitForTextDisplayed("Games in training: 1")
    }

    private fun assertBoardFenEventually(expectedFen: String) {
        // Do not replace this with a single immediate assert unless the screen's async behavior
        // is removed. On this screen the visible board can lag slightly behind the click because
        // selection, scroll, and board loading happen through Compose effects.
        val normalizedExpectedFen = normalizeFenForAssertion(expectedFen)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            currentBoardFen()?.let(::normalizeFenForAssertion) == normalizedExpectedFen
        }
        composeRule.onNodeWithTag(InteractiveChessBoardTestTag).assert(
            fenStateDescriptionMatcher(expectedFen)
        )
    }

    private fun waitForNodeDisplayed(tag: String) {
        // The node can exist in the semantics tree before it is actually stable and visible after
        // list scrolling. Waiting here removes a common source of flaky click failures.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag(tag).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForNodeDisplayedByContentDescription(contentDescription: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithContentDescription(contentDescription).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForTextDisplayed(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithText(text).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForTextInTree(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithText(text).fetchSemanticsNode()
                true
            }.getOrDefault(false)
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

    private fun setEditTrainingScreenContent(
        gamesForTraining: List<TrainingGameEditorItem>,
    ) {
        setEditTrainingScreenContent(
            gamesForTraining = gamesForTraining,
            onStartGameTrainingClick = ::ignoreStartGameTrainingClick,
        )
    }

    private fun setEditTrainingScreenContent(
        gamesForTraining: List<TrainingGameEditorItem>,
        onStartGameTrainingClick: (Long, List<Long>) -> Unit,
    ) {
        composeRule.setContent {
            ChessBoardTheme {
                EditTrainingScreen(
                    gamesForTraining = gamesForTraining,
                    orderGamesInTraining = RuntimeContext.OrderGamesInTraining(),
                    onBackClick = ::ignoreBackClick,
                    onNavigate = ::ignoreNavigate,
                    onStartGameTrainingClick = onStartGameTrainingClick,
                    onOpenGameEditorClick = ::ignoreOpenGameEditorClick,
                    onOpenSettingsClick = ::ignoreOpenSettingsClick,
                    onSaveTraining = ::ignoreSaveTraining,
                )
            }
        }
    }

    private companion object {
        fun ignoreBackClick() = Unit

        fun ignoreNavigate(screenType: com.example.chessboard.ui.screen.ScreenType) = Unit

        fun ignoreStartGameTrainingClick(gameId: Long, orderedGameIds: List<Long>) = Unit

        fun ignoreOpenGameEditorClick(gameId: Long) = Unit

        fun ignoreOpenSettingsClick() = Unit

        fun ignoreSaveTraining(
            trainingName: String,
            editableGames: List<TrainingGameEditorItem>,
            showSuccessMessage: Boolean,
            onSaved: (() -> Unit)?,
        ) {
            onSaved?.invoke()
        }

        val TestTrainingGame = TrainingGameEditorItem(
            gameId = 1L,
            title = "Test Opening",
            pgn = "1. e2e4 e7e5 *",
            sideMask = SideMask.WHITE
        )
        val SecondTrainingGame = TrainingGameEditorItem(
            gameId = 2L,
            title = "Second Opening",
            pgn = "1. d2d4 d7d5 *",
            sideMask = SideMask.WHITE
        )
        const val AfterE4Fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
    }
}
