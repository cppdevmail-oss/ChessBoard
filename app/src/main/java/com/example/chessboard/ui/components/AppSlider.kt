package com.example.chessboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingAccentTeal
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNumberSlider(
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = (value - min).toFloat() / (max - min).coerceAtLeast(1)

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "$value",
                color = TextColor.Primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.centeredAtFraction(fraction),
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min - 1).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = TrainingAccentTeal,
                activeTrackColor = TrainingAccentTeal,
                inactiveTrackColor = TrainingAccentTeal.copy(alpha = 0.24f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$min",
                color = TextColor.Secondary,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = "$max",
                color = TextColor.Secondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun Modifier.centeredAtFraction(fraction: Float): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        )
        val thumbRadiusPx = 10.dp.toPx()
        val trackWidthPx = constraints.maxWidth - 2f * thumbRadiusPx
        val centerPx = thumbRadiusPx + trackWidthPx * fraction
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative(
                x = (centerPx - placeable.width / 2f).roundToInt(),
                y = 0,
            )
        }
    }
