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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.ui.EditTrainingMoveLegendSectionTestTag
import com.example.chessboard.ui.MoveLegendNextTestTag
import com.example.chessboard.ui.TrainingEditorGameCardTestTag
import com.example.chessboard.ui.components.AppConfirmDialog
import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.BoardActionBlock
import com.example.chessboard.ui.components.BoardActionButtonGroup
import com.example.chessboard.ui.components.DeleteIconButton
import com.example.chessboard.ui.components.ChessBoardSection
import com.example.chessboard.ui.components.IconSm
import com.example.chessboard.ui.components.IconXs
import com.example.chessboard.ui.components.MoveSequenceSection
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingIconInactive

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

internal data class TrainingEditorPrimaryAction(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val contentDescription: String,
    val tint: Color = TextColor.Primary,
)

@Composable
internal fun TrainingEditorGameSection(
    state: TrainingEditorGameSectionState,
    actions: TrainingEditorGameSectionActions,
    primaryAction: TrainingEditorPrimaryAction? = null,
    primaryActions: List<TrainingEditorPrimaryAction> = emptyList(),
    removeCollectionLabel: String = "training",
    modifier: Modifier = Modifier,
) {
    val visiblePrimaryActions = resolveVisiblePrimaryActions(
        primaryAction = primaryAction,
        primaryActions = primaryActions,
    )
    var showRemoveConfirm by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(AppDimens.spaceLg))
        }

        TrainingEditorGameCard(
            state = state,
            actions = actions,
            primaryActions = visiblePrimaryActions,
        )
    }
}

private fun resolveVisiblePrimaryActions(
    primaryAction: TrainingEditorPrimaryAction?,
    primaryActions: List<TrainingEditorPrimaryAction>,
): List<TrainingEditorPrimaryAction> {
    if (primaryActions.isNotEmpty()) {
        return primaryActions
    }

    return listOfNotNull(primaryAction)
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

@Composable
private fun TrainingEditorGameCard(
    state: TrainingEditorGameSectionState,
    actions: TrainingEditorGameSectionActions,
    primaryActions: List<TrainingEditorPrimaryAction>,
) {
    val cardActionButtonSize = AppDimens.iconButtonSize
    val canUndo = state.isSelected && state.gameController.canUndo
    val canRedo = state.isSelected && state.gameController.canRedo

    BoardActionBlock(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TrainingEditorGameCardTestTag),
        highlighted = state.isSelected,
        onClick = if (state.isSelected) null else actions.onSelect
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainingEditorMoveControls(
                canUndo = canUndo,
                canRedo = canRedo,
                onResetClick = actions.onResetClick,
                onPrevClick = actions.onPrevClick,
                onNextClick = actions.onNextClick,
                buttonSize = cardActionButtonSize,
            )

            Spacer(modifier = Modifier.width(AppDimens.spaceSm))

            IconButton(
                onClick = actions.onEditGameClick,
                modifier = Modifier.size(cardActionButtonSize)
            ) {
                IconSm(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit game",
                )
            }

            RenderPrimaryGameActionButtons(
                primaryActions = primaryActions,
                buttonSize = cardActionButtonSize,
            )
        }

        if (state.parsedGame == null) {
            return@BoardActionBlock
        }

        Spacer(modifier = Modifier.height(AppDimens.spaceMd))
        HorizontalDivider(color = Background.ScreenDark, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppDimens.spaceMd))
        MoveSequenceSection(
            moveLabels = state.parsedGame.moveLabels,
            currentPly = state.currentPly,
            onMovePlyClick = { ply ->
                actions.onSelect()
                actions.onMovePlyClick(ply)
            },
            modifier = Modifier.testTag(EditTrainingMoveLegendSectionTestTag),
        )
    }
}

@Composable
private fun TrainingEditorMoveControls(
    canUndo: Boolean,
    canRedo: Boolean,
    onResetClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    buttonSize: Dp,
) {
    val undoTint = resolveTrainingEditorMoveControlTint(canUndo)
    val redoTint = resolveTrainingEditorMoveControlTint(canRedo)

    BoardActionButtonGroup {
        IconButton(
            onClick = onResetClick,
            enabled = canUndo,
            modifier = Modifier.size(buttonSize)
        ) {
            IconSm(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = undoTint,
            )
        }
        IconButton(
            onClick = onPrevClick,
            enabled = canUndo,
            modifier = Modifier.size(buttonSize)
        ) {
            IconSm(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous move",
                tint = undoTint,
            )
        }
        IconButton(
            onClick = onNextClick,
            enabled = canRedo,
            modifier = Modifier
                .size(buttonSize)
                .testTag(MoveLegendNextTestTag)
        ) {
            IconSm(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next move",
                tint = redoTint,
            )
        }
    }
}

private fun resolveTrainingEditorMoveControlTint(isEnabled: Boolean): Color {
    if (isEnabled) {
        return TextColor.Primary
    }

    return TrainingIconInactive
}

@Composable
private fun RenderPrimaryGameActionButtons(
    primaryActions: List<TrainingEditorPrimaryAction>,
    buttonSize: Dp,
) {
    if (primaryActions.isEmpty()) {
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        primaryActions.forEach { action ->
            IconButton(
                onClick = action.onClick,
                modifier = Modifier.size(buttonSize)
            ) {
                IconSm(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                    tint = action.tint,
                )
            }
        }
    }
}
