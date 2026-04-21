package com.example.chessboard.ui.screen.positions

/**
 * Navigation coverage for the saved positions entry point.
 *
 * Keep tests here focused on opening the screen and top-level saved-position flows.
 * Do not add low-level persistence or board interaction tests here.
 */
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chessboard.MainActivity
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.ui.SavedPositionsContentTestTag
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SavedPositionsNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val dbProvider: DatabaseProvider
        get() = DatabaseProvider.createInstance(composeRule.activity)

    @Before
    fun setUp() {
        dbProvider.clearAllData()
    }

    @Test
    fun home_savedPositionsCardOpensSavedPositionsScreen() {
        waitForTextDisplayed("Saved Positions")

        composeRule.onNodeWithText("Saved Positions").performClick()

        waitForNodeDisplayed(SavedPositionsContentTestTag)
        composeRule.onNodeWithText("No saved positions available.").assertIsDisplayed()
    }

    @Test
    fun savedPositionsScreen_displaysPersistedPositions() {
        runBlocking {
            dbProvider.createSavedSearchPositionService().create(
                name = "Italian Position",
                fenForSearch = InitialBoardFen,
                fenFull = InitialBoardFen,
            )
        }

        waitForTextDisplayed("Saved Positions")
        composeRule.onNodeWithText("Saved Positions").performClick()

        waitForTextDisplayed("Italian Position")
        composeRule.onNodeWithText("FEN: $InitialBoardFen").assertIsDisplayed()
    }

    private fun waitForTextDisplayed(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithText(text).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForNodeDisplayed(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag(testTag).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }
}
