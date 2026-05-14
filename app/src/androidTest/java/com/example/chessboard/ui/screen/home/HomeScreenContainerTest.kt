package com.example.chessboard.ui.screen.home

/**
 * File role: covers HomeScreenContainer navigation behavior that depends on app data.
 * Allowed here:
 * - integration-style Compose tests for home container callbacks and dialogs
 * - small database fixtures needed to drive home navigation decisions
 * Not allowed here:
 * - broad MainActivity navigation coverage
 * - low-level line-saving or training-service assertions
 * Validation date: 2026-05-14
 */
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.uciMovesToMoves
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.theme.ChessBoardTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenContainerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dbProvider: DatabaseProvider
        get() = DatabaseProvider.createInstance(composeRule.activity)

    @Before
    fun setUp() {
        dbProvider.clearAllData()
    }

    @Test
    fun simpleHome_smartTrainingClickWithoutLinesShowsCreateOpeningDialog() {
        var createOpeningClicks = 0

        setHomeContent(
            onCreateOpeningClick = {
                createOpeningClicks += 1
            },
        )

        waitForTextDisplayed("Smart Training")
        composeRule.onNodeWithText("Smart Training").performClick()

        waitForTextDisplayed("No openings yet")
        composeRule.onNodeWithText(
            "Create at least one opening or line before starting Smart Training."
        ).assertIsDisplayed()

        composeRule.onNodeWithText("Create Opening").performClick()

        composeRule.runOnIdle {
            check(createOpeningClicks == 1) {
                "Expected Create Opening callback to be called once, got $createOpeningClicks"
            }
        }
    }

    @Test
    fun simpleHome_smartTrainingClickWithLineNavigatesToSmartTraining() {
        saveLine()
        var navigatedScreen: ScreenType? = null

        setHomeContent(
            onNavigate = { screen ->
                navigatedScreen = screen
            },
        )

        waitForTextDisplayed("Smart Training")
        composeRule.onNodeWithText("Smart Training").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            navigatedScreen == ScreenType.SmartTraining
        }
        composeRule.onAllNodesWithText("No openings yet").fetchSemanticsNodes()
            .let { nodes ->
                check(nodes.isEmpty()) {
                    "Expected no missing-openings dialog after Smart Training navigation"
                }
            }
    }

    private fun setHomeContent(
        onNavigate: (ScreenType) -> Unit = {},
        onCreateOpeningClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            ChessBoardTheme {
                HomeScreenContainer(
                    activity = composeRule.activity,
                    screenContext = ScreenContainerContext(
                        onNavigate = onNavigate,
                        inDbProvider = dbProvider,
                    ),
                    simpleViewEnabled = true,
                    onCreateOpeningClick = onCreateOpeningClick,
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

    private fun saveLine() {
        runBlocking {
            val line = LineEntity(
                event = "Smart Training Source",
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
            append("[Event \"Smart Training Source\"]\n")
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
