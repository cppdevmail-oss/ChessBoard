package com.example.chessboard.ui.screen.analysis

/*
 * Localization holder for the line-analysis screen and its local controls.
 * Keep grouped resource reads and formatting helpers for analysis UI text here.
 * Do not add board behavior, PGN building, navigation, or persistence logic to this file.
 * Validation date: 2026-05-30
 */

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.chessboard.R

internal data class LineAnalysisStrings(
    val title: String,
    val subtitle: String,
    val copyPgnContentDescription: String,
    val searchByPositionContentDescription: String,
    val pgnClipLabel: String,
    val pgnCopiedTitle: String,
    val pgnCopiedMessage: String,
    val pgnUnavailableTitle: String,
    val pgnUnavailableMessage: String,
    val buildingPgnTitle: String,
    val buildingPgnMessage: String,
)

@Composable
internal fun lineAnalysisStrings(): LineAnalysisStrings {
    return LineAnalysisStrings(
        title = stringResource(R.string.line_analysis_title),
        subtitle = stringResource(R.string.line_analysis_subtitle),
        copyPgnContentDescription = stringResource(
            R.string.line_analysis_copy_pgn_content_description
        ),
        searchByPositionContentDescription = stringResource(
            R.string.line_analysis_search_by_position_content_description
        ),
        pgnClipLabel = stringResource(R.string.line_analysis_pgn_clip_label),
        pgnCopiedTitle = stringResource(R.string.line_analysis_pgn_copied_title),
        pgnCopiedMessage = stringResource(R.string.line_analysis_pgn_copied_message),
        pgnUnavailableTitle = stringResource(R.string.line_analysis_pgn_unavailable_title),
        pgnUnavailableMessage = stringResource(R.string.line_analysis_pgn_unavailable_message),
        buildingPgnTitle = stringResource(R.string.line_analysis_building_pgn_title),
        buildingPgnMessage = stringResource(R.string.line_analysis_building_pgn_message),
    )
}
