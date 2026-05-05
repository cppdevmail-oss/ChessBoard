package com.example.chessboard.ui.screen.createOpening

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.chessboard.R
import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.BoardActionNavigationBar
import com.example.chessboard.ui.components.BoardActionNavigationItem
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.screen.EditableGameSide
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingIconInactive

@Composable
internal fun CreateOpeningBoardControlsBar(
    selectedSide: EditableGameSide,
    onSideSelected: (EditableGameSide) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndoClick: () -> Unit,
    onResetClick: () -> Unit,
    onRedoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoardActionNavigationBar(
        modifier = modifier,
        items = EditableGameSide.entries.map { side ->
            BoardActionNavigationItem(
                label = if (side == EditableGameSide.AS_WHITE) "White" else "Black",
                selected = side == selectedSide,
                onClick = { onSideSelected(side) },
            ) {
                SideSymbolNavigationIcon(
                    side = side,
                    selected = side == selectedSide,
                )
            }
        } + listOf(
            BoardActionNavigationItem(
                label = "Reset",
                onClick = onResetClick,
            ) {
                IconMd(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset",
                    tint = TrainingIconInactive,
                )
            },
            BoardActionNavigationItem(
                label = "Back",
                enabled = canUndo,
                onClick = onUndoClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = if (canUndo) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                )
            },
            BoardActionNavigationItem(
                label = "Forward",
                enabled = canRedo,
                onClick = onRedoClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Forward",
                    tint = if (canRedo) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                )
            },
        ),
    )
}

@Composable
private fun SideSymbolNavigationIcon(
    side: EditableGameSide,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(R.drawable.ic_king),
        contentDescription = side.toDisplayText(),
        tint = if (selected) TrainingAccentTeal else TrainingIconInactive,
        modifier = modifier.size(AppIconSizes.Lg),
    )
}
