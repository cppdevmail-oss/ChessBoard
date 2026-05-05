package com.example.chessboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TrainingAccentTeal

/** Wraps board-adjacent actions in the standard highlighted action block. */
@Composable
fun BoardActionBlock(
    modifier: Modifier = Modifier,
    highlighted: Boolean = true,
    color: Color = if (highlighted) Background.CardDark else Background.SurfaceDark,
    borderColor: Color = TrainingAccentTeal,
    contentPadding: PaddingValues = PaddingValues(AppDimens.spaceMd),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CardSurface(
        modifier = modifier,
        color = color,
        border = if (highlighted) BorderStroke(1.dp, borderColor) else null,
        contentPadding = contentPadding,
        onClick = onClick,
        content = content,
    )
}

/** Wraps a compact row of related board action buttons in a dark pill. */
@Composable
fun BoardActionButtonGroup(
    modifier: Modifier = Modifier,
    color: Color = Background.ScreenDark,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.radiusPill),
        color = color
    ) {
        Row(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}
