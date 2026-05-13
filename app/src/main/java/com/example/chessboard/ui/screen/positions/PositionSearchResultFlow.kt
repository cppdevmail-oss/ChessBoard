package com.example.chessboard.ui.screen.positions

/**
 * Shared position-search result flow for screens that search lines by board position.
 *
 * Keep reusable found-lines dialogs and create-template helpers here. Do not add screen layout,
 * card rendering, or position-search board editing logic to this file.
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.ui.PositionTemplateNameConfirmTestTag
import com.example.chessboard.service.OneLineTrainingData
import com.example.chessboard.ui.PositionSearchResultShowLinesActionTestTag
import com.example.chessboard.ui.PositionSearchResultTemplateActionTestTag
import com.example.chessboard.ui.PositionSearchResultTrainingActionTestTag
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.AppMessageDialog
import com.example.chessboard.ui.components.AppMessageDialogAction
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.PrimaryButton
import com.example.chessboard.ui.components.ScreenTitleText
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background

internal const val PositionTemplateDefaultName = "Template From Position"

internal data class PositionTemplateNameDialogState(
    val lineIds: List<Long>,
    val templateName: String = PositionTemplateDefaultName,
)

internal data class PositionSearchResultDialogActions(
    val onDismiss: () -> Unit,
    val onCreateTrainingClick: () -> Unit,
    val onCreateTemplateClick: () -> Unit,
    val onShowLinesClick: (() -> Unit)? = null,
    val showTemplateAction: Boolean = true,
    val templateNameDialogState: PositionTemplateNameDialogState? = null,
    val onTemplateNameChange: (String) -> Unit = {},
    val onTemplateNameDismiss: () -> Unit = {},
    val onConfirmTemplateName: () -> Unit = {},
)

@Composable
internal fun RenderPositionSearchResultDialog(
    foundLineIds: List<Long>?,
    actions: PositionSearchResultDialogActions,
) {
    RenderPositionTemplateNameDialog(
        dialogState = actions.templateNameDialogState,
        onTemplateNameChange = actions.onTemplateNameChange,
        onDismiss = actions.onTemplateNameDismiss,
        onConfirm = actions.onConfirmTemplateName,
    )

    if (foundLineIds == null) {
        return
    }

    if (foundLineIds.isEmpty()) {
        AppMessageDialog(
            title = resolveFoundLineIdsTitle(foundLineIds),
            message = resolveFoundLineIdsMessage(foundLineIds),
            onDismiss = actions.onDismiss,
        )
        return
    }

    val resultActions = buildList {
        actions.onShowLinesClick?.let { onShowLinesClick ->
            add(
                AppMessageDialogAction(
                    text = "Show Lines",
                    onClick = onShowLinesClick,
                    testTag = PositionSearchResultShowLinesActionTestTag,
                )
            )
        }
        if (actions.showTemplateAction) {
            add(
                AppMessageDialogAction(
                    text = "Template",
                    onClick = actions.onCreateTemplateClick,
                    testTag = PositionSearchResultTemplateActionTestTag,
                )
            )
        }
        add(
            AppMessageDialogAction(
                text = "Training",
                onClick = actions.onCreateTrainingClick,
                testTag = PositionSearchResultTrainingActionTestTag,
            )
        )
    }

    AppMessageDialog(
        title = resolveFoundLineIdsTitle(foundLineIds),
        message = resolveFoundLineIdsMessage(foundLineIds),
        onDismiss = actions.onDismiss,
        actions = resultActions,
    )
}

internal suspend fun createPositionTemplateFromLineIds(
    dbProvider: DatabaseProvider,
    lineIds: List<Long>,
    templateName: String = PositionTemplateDefaultName,
): Long? {
    val templateService = dbProvider.createTrainingTemplateService()
    val templateId = templateService.createTemplate(
        resolvePositionTemplateName(templateName)
    )
    if (templateId <= 0L) {
        return null
    }

    val allLinesAdded = lineIds.all { lineId ->
        templateService.addLine(
            templateId = templateId,
            lineId = lineId,
            weight = OneLineTrainingData(lineId = lineId, weight = 1).weight,
        )
    }

    return templateId.takeIf { allLinesAdded }
}

@Composable
private fun RenderPositionTemplateNameDialog(
    dialogState: PositionTemplateNameDialogState?,
    onTemplateNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val currentState = dialogState ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background.ScreenDark,
        title = {
            ScreenTitleText(text = "New Template")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)) {
                BodySecondaryText(
                    text = "Name the template created from the matched lines."
                )
                AppTextField(
                    value = currentState.templateName,
                    onValueChange = onTemplateNameChange,
                    label = "Template Name",
                    placeholder = PositionTemplateDefaultName,
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Template",
                onClick = onConfirm,
                modifier = Modifier.testTag(PositionTemplateNameConfirmTestTag),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                CardMetaText(text = "Cancel")
            }
        },
    )
}

private fun resolveFoundLineIdsTitle(foundLineIds: List<Long>): String {
    if (foundLineIds.isEmpty()) {
        return "Lines Not Found"
    }

    return "Lines Found"
}

private fun resolveFoundLineIdsMessage(foundLineIds: List<Long>): String {
    if (foundLineIds.isEmpty()) {
        return "No saved lines contain this position."
    }

    return "${foundLineIds.size} saved lines match this position. Choose what to create from them."
}

private fun resolvePositionTemplateName(templateName: String): String {
    val normalizedName = templateName.trim()
    if (normalizedName.isBlank()) {
        return PositionTemplateDefaultName
    }

    return normalizedName
}
