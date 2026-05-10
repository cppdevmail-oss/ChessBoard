package com.example.chessboard.ui.screen

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.LineBackupRestoreProgress
import com.example.chessboard.service.LineBackupRestoreResult
import com.example.chessboard.ui.BackupRestoreCancelTestTag
import com.example.chessboard.ui.BackupRestoreProgressDialogTestTag
import com.example.chessboard.ui.theme.ChessBoardTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.junit.Rule
import org.junit.Test

class BackupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backupScreen_restoreShowsProgressAndCanBeCanceledAtFifthLine() {
        val fakeRestoreUri = Uri.parse("content://test/backup.pgn")
        val waitForCancelAtFifthLine = CompletableDeferred<Unit>()

        composeRule.setContent {
            ChessBoardTheme {
                BackupScreenContainer(
                    activity = composeRule.activity,
                    screenContext = ScreenContainerContext(
                        inDbProvider = DatabaseProvider.createInstance(composeRule.activity)
                    ),
                    testRestoreUri = fakeRestoreUri,
                    restoreBackupRunner = { _, onProgress ->
                        val totalLines = 15
                        var processedLinesCount = 0
                        var restoredLinesCount = 0
                        var skippedLinesCount = 0

                        onProgress(
                            LineBackupRestoreProgress(
                                totalLines = totalLines,
                                processedLinesCount = processedLinesCount,
                                restoredLinesCount = restoredLinesCount,
                                skippedLinesCount = skippedLinesCount,
                            )
                        )

                        repeat(totalLines) {
                            currentCoroutineContext().ensureActive()
                            processedLinesCount += 1
                            restoredLinesCount += 1
                            onProgress(
                                LineBackupRestoreProgress(
                                    totalLines = totalLines,
                                    processedLinesCount = processedLinesCount,
                                    restoredLinesCount = restoredLinesCount,
                                    skippedLinesCount = skippedLinesCount,
                                )
                            )

                            if (processedLinesCount == 5) {
                                waitForCancelAtFifthLine.await()
                            }
                        }

                        LineBackupRestoreResult(
                            restoredLinesCount = restoredLinesCount,
                            skippedLinesCount = skippedLinesCount
                        )
                    }
                )
            }
        }

        composeRule.onNodeWithText("Restore Lines").performClick()
        composeRule.onNodeWithText("Restore").performClick()

        waitForNodeWithTag(BackupRestoreProgressDialogTestTag)
        waitForText("Processed lines: 5/15")
        composeRule.onNodeWithTag(BackupRestoreCancelTestTag).performClick()

        waitForText("Restore canceled.")
        waitForText("Processed lines: 5/15")
        waitForNodeWithTagToDisappear(BackupRestoreProgressDialogTestTag)
    }

    private fun waitForNodeWithTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithTag(tag).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForNodeWithTagToDisappear(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithTag(tag).fetchSemanticsNode()
                false
            }.getOrDefault(true)
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText(text, substring = true).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
    }
}
