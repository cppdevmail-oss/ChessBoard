package com.example.chessboard.ui.screen.training.common

/*
 * Shared per-line editor UI for training-like collection screens.
 *
 * Keep the visual section for one editable line here, including title, weight
 * controls, preview board, and move navigation card. Do not add screen-level
 * loading, save flows, or list scaffolds to this file.
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.chessboard.boardmodel.LineController
import com.example.chessboard.ui.EditTrainingMoveLegendSectionTestTag
import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.ChessBoardSection
import com.example.chessboard.ui.components.IconXs
import com.example.chessboard.ui.components.LineMoveTreeSection
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingAccentTeal

internal data class TrainingEditorLineSectionState(
    val line: TrainingLineEditorItem,
    val parsedLine: ParsedTrainingEditorLine?,
    val isSelected: Boolean,
    val lineController: LineController,
    val currentPly: Int,
    val simpleViewEnabled: Boolean = false,
)

internal data class TrainingEditorLineSectionActions(
    val onDecreaseWeightClick: () -> Unit,
    val onIncreaseWeightClick: () -> Unit,
    val onSelect: () -> Unit,
    val onPrevClick: () -> Unit,
    val onNextClick: () -> Unit,
    val onResetClick: () -> Unit,
    val onEditLineClick: () -> Unit,
    val onMovePlyClick: (Int) -> Unit,
)

@Composable
internal fun TrainingEditorLineSection(
    state: TrainingEditorLineSectionState,
    actions: TrainingEditorLineSectionActions,
    modifier: Modifier = Modifier,
) {
    val displayController = remember { LineController() }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TrainingEditorLineHeader(
            line = state.line,
            simpleViewEnabled = state.simpleViewEnabled,
            onDecreaseWeightClick = actions.onDecreaseWeightClick,
            onIncreaseWeightClick = actions.onIncreaseWeightClick,
            onSelect = if (!state.isSelected) actions.onSelect else null,
        )

        Spacer(modifier = Modifier.height(AppDimens.spaceSm))

        if (state.isSelected && state.parsedLine != null) {
            ChessBoardSection(lineController = state.lineController)
            Spacer(modifier = Modifier.height(AppDimens.spaceMd))
            LineMoveTreeSection(
                importedUciLines = listOf(state.parsedLine.uciMoves),
                lineController = state.lineController,
                modifier = Modifier.testTag(EditTrainingMoveLegendSectionTestTag),
            )
        } else if (state.parsedLine != null) {
            LineMoveTreeSection(
                importedUciLines = listOf(state.parsedLine.uciMoves),
                lineController = displayController,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditTrainingMoveLegendSectionTestTag),
                onMoveSelected = { _, ply -> actions.onMovePlyClick(ply) },
            )
        }
    }
}

@Composable
private fun TrainingEditorLineHeader(
    line: TrainingLineEditorItem,
    simpleViewEnabled: Boolean,
    onDecreaseWeightClick: () -> Unit,
    onIncreaseWeightClick: () -> Unit,
    onSelect: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val titleModifier = if (onSelect != null) {
            Modifier.weight(1f).clickable(onClick = onSelect)
        } else {
            Modifier.weight(1f)
        }
        Column(modifier = titleModifier) {
            Text(
                text = line.title,
                color = TextColor.Primary,
                style = MaterialTheme.typography.titleMedium
            )
            RenderTrainingLineEcoBadge(line.eco)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!simpleViewEnabled) {
                IconButton(
                    onClick = onDecreaseWeightClick,
                    modifier = Modifier.size(AppIconSizes.Lg)
                ) {
                    IconXs(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = TrainingAccentTeal,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${line.weight}",
                        color = TextColor.Primary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "reps",
                        color = TextColor.Secondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(
                    onClick = onIncreaseWeightClick,
                    modifier = Modifier.size(AppIconSizes.Lg)
                ) {
                    IconXs(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = TrainingAccentTeal,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderTrainingLineEcoBadge(eco: String?) {
    if (eco.isNullOrBlank()) {
        return
    }

    Spacer(modifier = Modifier.height(2.dp))
    Surface(
        shape = RoundedCornerShape(50),
        color = TrainingAccentTeal.copy(alpha = 0.15f)
    ) {
        Text(
            text = eco,
            color = TrainingAccentTeal,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = AppDimens.spaceSm, vertical = 3.dp)
        )
    }
}
