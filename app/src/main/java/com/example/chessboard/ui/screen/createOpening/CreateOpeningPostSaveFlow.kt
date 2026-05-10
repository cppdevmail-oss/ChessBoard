package com.example.chessboard.ui.screen.createOpening

/**
 * Post-save flow for actions offered after opening lines are saved.
 *
 * Keep in this file:
 * - state models for the post-save flow
 * - dialog sequencing for optional training/template creation
 * - helper functions that execute training/template creation after save
 *
 * It is acceptable to add here:
 * - new post-save steps related to saved opening lines
 * - flow-specific validation or warning aggregation
 * - small helper functions used only by this flow
 *
 * Do not add here:
 * - the main create-opening screen layout
 * - unrelated screen container state
 * - generic app-wide dialog abstractions or unrelated database helpers
 */
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.OneLineTrainingData
import com.example.chessboard.ui.components.AppConfirmDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SavedOpeningLines(
    val name: String,
    val lineIds: List<Long>,
)

enum class PostSaveDialogStep {
    CreateTraining,
    CreateTemplate,
}

data class CreateOpeningPostSaveState(
    val pendingSavedLines: SavedOpeningLines? = null,
    val dialogStep: PostSaveDialogStep? = null,
    val warningMessage: String? = null,
)

fun startCreateOpeningPostSaveFlow(
    openingName: String,
    savedLineIds: List<Long>,
): CreateOpeningPostSaveState {
    return CreateOpeningPostSaveState(
        pendingSavedLines = SavedOpeningLines(
            name = openingName.ifBlank { "Opening" },
            lineIds = savedLineIds,
        ),
        dialogStep = PostSaveDialogStep.CreateTraining,
    )
}

private fun advanceCreateOpeningPostSaveFlowToTemplate(
    state: CreateOpeningPostSaveState,
): CreateOpeningPostSaveState {
    if (state.pendingSavedLines == null) {
        return state.copy(dialogStep = null)
    }
    return state.copy(dialogStep = PostSaveDialogStep.CreateTemplate)
}

private fun appendCreateOpeningPostSaveWarning(
    state: CreateOpeningPostSaveState,
    message: String,
): CreateOpeningPostSaveState {
    val updatedWarning = if (state.warningMessage.isNullOrBlank()) {
        message
    } else {
        state.warningMessage + "\n" + message
    }
    return state.copy(warningMessage = updatedWarning)
}

internal suspend fun createOpeningTraining(
    dbProvider: DatabaseProvider,
    savedLines: SavedOpeningLines,
): Boolean {
    val trainingId = dbProvider.createTrainingService().createTrainingFromLines(
        name = savedLines.name,
        lines = savedLines.lineIds.map { lineId ->
            OneLineTrainingData(lineId = lineId, weight = 1)
        },
    )
    return trainingId != null
}

private suspend fun createOpeningTemplate(
    dbProvider: DatabaseProvider,
    savedLines: SavedOpeningLines,
): Boolean {
    val templateService = dbProvider.createTrainingTemplateService()
    val templateId = templateService.createTemplate(savedLines.name)
    if (templateId <= 0L) {
        return false
    }

    return savedLines.lineIds.all { lineId ->
        templateService.addLine(templateId = templateId, lineId = lineId, weight = 1)
    }
}

@Composable
fun CreateOpeningPostSaveDialogs(
    activity: Activity,
    dbProvider: DatabaseProvider,
    state: CreateOpeningPostSaveState,
    onStateChange: (CreateOpeningPostSaveState) -> Unit,
    onFinished: () -> Unit,
    onError: (String) -> Unit,
) {
    val savedLines = state.pendingSavedLines ?: return
    val savedLinesCount = savedLines.lineIds.size
    val trainingMessage = if (savedLinesCount == 1) {
        "Do you want to create a training from the saved line?"
    } else {
        "Do you want to create a training from the $savedLinesCount saved lines?"
    }
    val templateMessage = if (savedLinesCount == 1) {
        "Do you want to create a template from the saved line?"
    } else {
        "Do you want to create a template from the $savedLinesCount saved lines?"
    }

    if (state.dialogStep == PostSaveDialogStep.CreateTraining) {
        AppConfirmDialog(
            title = "Create Training",
            message = trainingMessage,
            onDismiss = {
                onStateChange(advanceCreateOpeningPostSaveFlowToTemplate(state))
            },
            onConfirm = {
                onStateChange(state.copy(dialogStep = null))
                (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
                    var nextState = state
                    if (!createOpeningTraining(dbProvider, savedLines)) {
                        nextState = appendCreateOpeningPostSaveWarning(
                            state = nextState,
                            message = "Lines were saved, but training could not be created",
                        )
                    }
                    nextState = advanceCreateOpeningPostSaveFlowToTemplate(nextState)

                    withContext(Dispatchers.Main) {
                        onStateChange(nextState)
                    }
                }
            },
            confirmText = "Create",
            dismissText = "Skip",
        )
    }

    if (state.dialogStep == PostSaveDialogStep.CreateTemplate) {
        AppConfirmDialog(
            title = "Create Template",
            message = templateMessage,
            onDismiss = {
                if (state.warningMessage.isNullOrBlank()) {
                    onFinished()
                } else {
                    onError(state.warningMessage)
                }
            },
            onConfirm = {
                onStateChange(state.copy(dialogStep = null))
                (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
                    var nextState = state
                    if (!createOpeningTemplate(dbProvider, savedLines)) {
                        nextState = appendCreateOpeningPostSaveWarning(
                            state = nextState,
                            message = "Lines were saved, but template could not be created",
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (nextState.warningMessage.isNullOrBlank()) {
                            onFinished()
                        } else {
                            onError(nextState.warningMessage)
                        }
                    }
                }
            },
            confirmText = "Create",
            dismissText = "Skip",
        )
    }
}
