package com.example.chessboard.ui.screen.positions

/*
 * Localization holder for shared position-search result dialogs.
 * Keep grouped resource reads and formatting helpers for found-lines dialogs here.
 * Do not add screen-specific layout, database access, or navigation behavior to this file.
 * Validation date: 2026-05-29
 */

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.chessboard.R

internal class PositionSearchResultStrings(
    private val resources: Resources,
    val showLinesAction: String,
    val templateAction: String,
    val trainingAction: String,
    val newTemplateTitle: String,
    val templateNamePrompt: String,
    val templateNameLabel: String,
    val templateDefaultName: String,
    val linesNotFoundTitle: String,
    val linesFoundTitle: String,
    val noLinesFoundMessage: String,
    val cancelAction: String,
) {
    fun foundLinesMessage(lineCount: Int): String {
        return resources.getQuantityString(
            R.plurals.position_search_found_lines_message,
            lineCount,
            lineCount,
        )
    }
}

@Composable
internal fun positionSearchResultStrings(): PositionSearchResultStrings {
    return PositionSearchResultStrings(
        resources = LocalContext.current.resources,
        showLinesAction = stringResource(R.string.position_search_result_show_lines_action),
        templateAction = stringResource(R.string.position_search_result_template_action),
        trainingAction = stringResource(R.string.position_search_result_training_action),
        newTemplateTitle = stringResource(R.string.position_search_new_template_title),
        templateNamePrompt = stringResource(R.string.position_search_template_name_prompt),
        templateNameLabel = stringResource(R.string.position_search_template_name_label),
        templateDefaultName = stringResource(R.string.position_search_template_default_name),
        linesNotFoundTitle = stringResource(R.string.position_search_lines_not_found_title),
        linesFoundTitle = stringResource(R.string.position_search_lines_found_title),
        noLinesFoundMessage = stringResource(R.string.position_search_no_lines_found_message),
        cancelAction = stringResource(R.string.common_cancel),
    )
}
