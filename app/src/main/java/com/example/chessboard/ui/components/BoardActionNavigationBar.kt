package com.example.chessboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingIconInactive

data class BoardActionNavigationItem(
    val label: String,
    val modifier: Modifier = Modifier,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
    val content: @Composable () -> Unit,
)

@Composable
fun BoardActionNavigationBar(
    items: List<BoardActionNavigationItem>,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5,
    showTopDivider: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Background.SurfaceDark,
        tonalElevation = 8.dp,
    ) {
        Column {
            if (showTopDivider) {
                AppDivider()
            }

            BoxWithConstraints {
                val normalizedMaxVisibleItems = maxVisibleItems.coerceAtLeast(1)
                val itemsCount = items.size.coerceAtLeast(1)
                val visibleItemsCount = itemsCount.coerceAtMost(normalizedMaxVisibleItems)
                val itemWidth = maxWidth / visibleItemsCount

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = AppDimens.spaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        BoardActionNavigationButton(
                            item = item,
                            itemWidth = itemWidth,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardActionNavigationButton(
    item: BoardActionNavigationItem,
    itemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val color = resolveBoardActionNavigationItemColor(
        selected = item.selected,
        enabled = item.enabled,
    )

    Column(
        modifier = modifier
            .then(item.modifier)
            .width(itemWidth)
            .clickable(enabled = item.enabled) { item.onClick() }
            .padding(AppDimens.spaceSm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(AppIconSizes.Lg + AppDimens.spaceXs),
            contentAlignment = Alignment.Center,
        ) {
            item.content()
        }
        Spacer(modifier = Modifier.height(AppDimens.spaceXs))
        NavLabelText(
            text = item.label,
            fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun resolveBoardActionNavigationItemColor(
    selected: Boolean,
    enabled: Boolean,
): Color {
    if (!enabled) {
        return TrainingIconInactive.copy(alpha = 0.5f)
    }

    return if (selected) TrainingAccentTeal else TrainingIconInactive
}
