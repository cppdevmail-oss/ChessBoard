package com.example.chessboard.ui.screen.positions.positionSearch

/*
 * Localization holder for PositionSearch screen orchestration strings.
 * Keep grouped resource reads and small formatting helpers used by this screen here.
 * Do not add persistence, navigation, or UI layout logic to this file.
 * Validation date: 2026-05-29
 */

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.chessboard.R
import com.example.chessboard.ui.screen.EditableLineSide

internal class PositionSearchStrings(
    private val resources: Resources,
    val failedApplyFenMessage: String,
    val templateCreatedTitle: String,
    private val templateCreatedMessageFormat: String,
    val templateErrorTitle: String,
    val templateErrorMessage: String,
    val duplicateNameMessage: String,
    val duplicateSearchFenMessage: String,
    val duplicateFullFenMessage: String,
    val failedSavePositionMessage: String,
    val positionNameRequiredMessage: String,
    val positionSavedTitle: String,
    private val positionSavedMessageFormat: String,
    val simpleTrainingName: String,
    val trainingCreatedTitle: String,
    val trainingErrorTitle: String,
    val trainingErrorMessage: String,
    val createTrainingScreenTitle: String,
    val linesFoundCountLabel: String,
    val defaultTemplateName: String,
) {
    fun templateCreatedMessage(
        templateId: Long,
        lineCount: Int,
    ): String {
        return templateCreatedMessageFormat.format(templateId, lineCount)
    }

    fun positionSavedMessage(
        positionName: String,
        positionId: Long,
    ): String {
        return positionSavedMessageFormat.format(positionName, positionId)
    }

    fun trainingCreatedMessage(lineCount: Int): String {
        return resources.getQuantityString(
            R.plurals.position_search_training_created_message,
            lineCount,
            lineCount,
        )
    }
}

internal data class PositionSearchBoardControlStrings(
    val whiteLabel: String,
    val blackLabel: String,
    val resetLabel: String,
    val clearLabel: String,
    val backLabel: String,
    val forwardLabel: String,
    val initialPositionContentDescription: String,
    val clearBoardContentDescription: String,
) {
    fun sideLabel(side: EditableLineSide): String {
        if (side == EditableLineSide.AS_BLACK) {
            return blackLabel
        }

        return whiteLabel
    }
}

@Composable
internal fun positionSearchStrings(): PositionSearchStrings {
    return PositionSearchStrings(
        resources = LocalContext.current.resources,
        failedApplyFenMessage = stringResource(R.string.position_search_failed_apply_fen),
        templateCreatedTitle = stringResource(R.string.position_search_template_created_title),
        templateCreatedMessageFormat = stringResource(R.string.position_search_template_created_message),
        templateErrorTitle = stringResource(R.string.position_search_template_error_title),
        templateErrorMessage = stringResource(R.string.position_search_template_error_message),
        duplicateNameMessage = stringResource(R.string.position_search_duplicate_name),
        duplicateSearchFenMessage = stringResource(R.string.position_search_duplicate_search_fen),
        duplicateFullFenMessage = stringResource(R.string.position_search_duplicate_full_fen),
        failedSavePositionMessage = stringResource(R.string.position_search_failed_save_position),
        positionNameRequiredMessage = stringResource(R.string.position_search_position_name_required),
        positionSavedTitle = stringResource(R.string.position_search_position_saved_title),
        positionSavedMessageFormat = stringResource(R.string.position_search_position_saved_message),
        simpleTrainingName = stringResource(R.string.position_search_training_name_from_position),
        trainingCreatedTitle = stringResource(R.string.position_search_training_created_title),
        trainingErrorTitle = stringResource(R.string.position_search_training_error_title),
        trainingErrorMessage = stringResource(R.string.position_search_training_error_message),
        createTrainingScreenTitle = stringResource(R.string.position_search_create_training_screen_title),
        linesFoundCountLabel = stringResource(R.string.position_search_lines_found_count_label),
        defaultTemplateName = stringResource(R.string.position_search_template_default_name),
    )
}

@Composable
internal fun positionSearchBoardControlStrings(): PositionSearchBoardControlStrings {
    return PositionSearchBoardControlStrings(
        whiteLabel = stringResource(R.string.position_search_side_white),
        blackLabel = stringResource(R.string.position_search_side_black),
        resetLabel = stringResource(R.string.common_reset),
        clearLabel = stringResource(R.string.position_search_clear),
        backLabel = stringResource(R.string.common_back),
        forwardLabel = stringResource(R.string.common_forward),
        initialPositionContentDescription = stringResource(
            R.string.position_search_initial_position_content_description
        ),
        clearBoardContentDescription = stringResource(
            R.string.position_search_clear_board_content_description
        ),
    )
}
