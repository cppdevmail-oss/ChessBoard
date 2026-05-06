package com.example.chessboard.ui.screen.training.common

/*
 * Shared per-game editor UI for training-like collection screens.
 *
 * Keep the visual section for one editable game here, including title, weight
 * controls, preview board, and move navigation card. Do not add screen-level
 * loading, save flows, or list scaffolds to this file.
 */

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.ui.EditTrainingMoveLegendSectionTestTag
import com.example.chessboard.ui.components.AppConfirmDialog
import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.DeleteIconButton
import com.example.chessboard.ui.components.ChessBoardSection
import com.example.chessboard.ui.components.IconXs
import com.example.chessboard.ui.components.GameMoveTreeSection
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingAccentTeal

internal data class TrainingEditorGameSectionState(
    val game: TrainingGameEditorItem,
    val parsedGame: ParsedTrainingEditorGame?,
    val isSelected: Boolean,
    val gameController: GameController,
    val currentPly: Int,
    val simpleViewEnabled: Boolean = false,
)

internal data class TrainingEditorGameSectionActions(
    val onDecreaseWeightClick: () -> Unit,
    val onIncreaseWeightClick: () -> Unit,
    val onSelect: () -> Unit,
    val onPrevClick: () -> Unit,
    val onNextClick: () -> Unit,
    val onResetClick: () -> Unit,
    val onEditGameClick: () -> Unit,
    val onMovePlyClick: (Int) -> Unit,
    val onRemoveClick: (() -> Unit)? = null,
)

@Composable
internal fun TrainingEditorGameSection(
    state: TrainingEditorGameSectionState,
    actions: TrainingEditorGameSectionActions,
    removeCollectionLabel: String = "training",
    modifier: Modifier = Modifier,
) {
    var showRemoveConfirm by remember { mutableStateOf(false) }
    val displayController = remember { GameController() }

    if (showRemoveConfirm) {
        AppConfirmDialog(
            title = "Remove Game",
            message = "Remove \"${state.game.title}\" from $removeCollectionLabel?",
            onDismiss = { showRemoveConfirm = false },
            onConfirm = {
                showRemoveConfirm = false
                actions.onRemoveClick?.invoke()
            },
            confirmText = "Remove",
            isDestructive = true,
        )
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        TrainingEditorGameHeader(
            game = state.game,
            simpleViewEnabled = state.simpleViewEnabled,
            onDecreaseWeightClick = actions.onDecreaseWeightClick,
            onIncreaseWeightClick = actions.onIncreaseWeightClick,
            onRemoveClick = if (actions.onRemoveClick != null) {
                { showRemoveConfirm = true }
            } else null,
            removeCollectionLabel = removeCollectionLabel,
        )

        Spacer(modifier = Modifier.height(AppDimens.spaceSm))

        if (state.isSelected && state.parsedGame != null) {
            ChessBoardSection(gameController = state.gameController)
            Spacer(modifier = Modifier.height(AppDimens.spaceMd))
            GameMoveTreeSection(
                importedUciLines = listOf(state.parsedGame.uciMoves),
                gameController = state.gameController,
                modifier = Modifier.testTag(EditTrainingMoveLegendSectionTestTag),
            )
        } else if (state.parsedGame != null) {
            GameMoveTreeSection(
                importedUciLines = listOf(state.parsedGame.uciMoves),
                gameController = displayController,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(EditTrainingMoveLegendSectionTestTag),
                onMoveSelected = { _, ply -> actions.onMovePlyClick(ply) },
            )
        }
    }
}

@Composable
private fun TrainingEditorGameHeader(
    game: TrainingGameEditorItem,
    simpleViewEnabled: Boolean,
    onDecreaseWeightClick: () -> Unit,
    onIncreaseWeightClick: () -> Unit,
    onRemoveClick: (() -> Unit)? = null,
    removeCollectionLabel: String = "training",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.title,
                color = TextColor.Primary,
                style = MaterialTheme.typography.titleMedium
            )
            RenderTrainingGameEcoBadge(game.eco)
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
                        text = "${game.weight}",
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
            if (onRemoveClick != null) {
                DeleteIconButton(
                    onClick = onRemoveClick,
                    contentDescription = "Remove game from $removeCollectionLabel",
                    modifier = Modifier.size(AppIconSizes.Lg),
                )
            }
        }
    }
}

@Composable
private fun RenderTrainingGameEcoBadge(eco: String?) {
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

