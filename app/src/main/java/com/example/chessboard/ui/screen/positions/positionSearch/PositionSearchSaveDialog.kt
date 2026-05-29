package com.example.chessboard.ui.screen.positions.positionSearch

/*
 * Screen-specific save-position dialog for PositionSearch.
 *
 * Keep in this file:
 * - UI-only dialog rendering for naming a saved position
 * - small dialog-local layout details tied to the PositionSearch save flow
 *
 * Do not add here:
 * - database calls or save orchestration
 * - navigation logic
 * - reusable generic dialog components that belong in ui/components
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.chessboard.R
import com.example.chessboard.ui.PositionSearchSaveNameFieldTestTag
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.PrimaryButton
import com.example.chessboard.ui.components.ScreenTitleText
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TrainingErrorRed

@Composable
internal fun RenderPositionSearchSaveDialog(
    saveDialogState: PositionSearchSaveDialogState?,
    actions: PositionSearchScreenActions.SaveDialog
) {
    val currentState = saveDialogState ?: return

    AlertDialog(
        onDismissRequest = actions.onDismiss,
        title = {
            ScreenTitleText(text = stringResource(R.string.position_search_save_position_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)) {
                BodySecondaryText(
                    text = stringResource(R.string.position_search_save_position_prompt)
                )
                AppTextField(
                    value = currentState.positionName,
                    onValueChange = actions.onPositionNameChange,
                    label = stringResource(R.string.position_search_position_name_label),
                    placeholder = stringResource(R.string.position_search_position_name_placeholder),
                    isError = currentState.errorMessage != null,
                    inputTestTag = PositionSearchSaveNameFieldTestTag
                )
                if (currentState.errorMessage != null) {
                    BodySecondaryText(
                        text = currentState.errorMessage,
                        color = TrainingErrorRed
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.position_search_save_action),
                onClick = actions.onConfirm
            )
        },
        dismissButton = {
            TextButton(onClick = actions.onDismiss) {
                CardMetaText(text = stringResource(R.string.common_cancel))
            }
        },
        containerColor = Background.ScreenDark,
    )
}
