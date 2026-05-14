package com.example.chessboard.ui.screen.home

/**
 * File role: owns the Home-screen Smart Training pre-navigation workflow.
 * Allowed here:
 * - temporary UI state for preparing Smart Training navigation from Home
 * - blocking preparation dialogs and no-lines explanation dialogs
 * - screen-level checks needed before leaving Home for Smart Training
 * Not allowed here:
 * - SimpleView or regular Home layout
 * - SmartTraining screen internals
 * - persistence rules beyond calling an injected screen service
 * Validation date: 2026-05-14
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.service.LineListService
import com.example.chessboard.ui.components.AppMessageDialog
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.SectionTitleText
import com.example.chessboard.ui.error.AppErrorReporter
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TrainingAccentTeal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class SmartTrainingNavigationState(
    val job: Job? = null,
    val requestId: Int = 0,
    val showNoLinesDialog: Boolean = false,
)

@Composable
internal fun HomeSmartTrainingNavigationHost(
    lineListService: LineListService,
    errorReporter: AppErrorReporter,
    onSmartTrainingClick: () -> Unit,
    onCreateOpeningClick: () -> Unit,
    content: @Composable (onSmartTrainingClick: () -> Unit) -> Unit,
) {
    var state by remember { mutableStateOf(SmartTrainingNavigationState()) }
    val scope = rememberCoroutineScope()

    fun cancelSmartTrainingPreparation() {
        val currentJob = state.job
        state = state.copy(
            job = null,
            requestId = state.requestId + 1,
        )
        currentJob?.cancel()
    }

    fun prepareSmartTrainingNavigation() {
        if (state.job != null) {
            return
        }

        val requestId = state.requestId + 1
        state = state.copy(
            requestId = requestId,
            showNoLinesDialog = false,
        )
        val job = scope.launch {
            try {
                val linesCount = withContext(Dispatchers.IO) {
                    lineListService.getLinesCount()
                }
                if (state.requestId != requestId) {
                    return@launch
                }

                state = state.copy(job = null)
                if (linesCount > 0) {
                    onSmartTrainingClick()
                    return@launch
                }

                state = state.copy(showNoLinesDialog = true)
            } catch (_: CancellationException) {
                if (state.requestId == requestId) {
                    state = state.copy(job = null)
                }
            } catch (error: Throwable) {
                if (state.requestId == requestId) {
                    state = state.copy(job = null)
                    errorReporter.report(
                        error = error,
                        message = "Failed to prepare Smart Training.",
                    )
                }
            }
        }
        state = state.copy(job = job)
    }

    content(::prepareSmartTrainingNavigation)

    if (state.job != null) {
        SmartTrainingPreparationDialog(onCancel = ::cancelSmartTrainingPreparation)
    }

    if (state.showNoLinesDialog) {
        AppMessageDialog(
            title = "No openings yet",
            message = "Create at least one opening or line before starting Smart Training.",
            confirmText = "Create Opening",
            onConfirm = {
                state = state.copy(showNoLinesDialog = false)
                onCreateOpeningClick()
            },
            dismissText = "Cancel",
            onDismiss = {
                state = state.copy(showNoLinesDialog = false)
            },
        )
    }
}

@Composable
private fun SmartTrainingPreparationDialog(
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Background.ScreenDark,
        title = {
            SectionTitleText(text = "Preparing Smart Training")
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceMd),
            ) {
                CircularProgressIndicator(color = TrainingAccentTeal)
                BodySecondaryText(
                    text = "Checking available openings...",
                    modifier = Modifier.padding(top = AppDimens.spaceXs),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                CardMetaText(text = "Cancel")
            }
        },
    )
}
