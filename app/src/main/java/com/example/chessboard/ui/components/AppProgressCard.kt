package com.example.chessboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingAccentTeal

private val ProgressCardBg = Color(0xFF141414)
private val ProgressCardTrack = Color(0xFF222222)

@Composable
fun AppProgressCard(
    label: String,
    progress: Int,
    total: Int,
    progressLabel: String = "",
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return

    val fraction = (progress.toFloat() / total).coerceIn(0f, 1f)
    val countText = if (progressLabel.isNotBlank()) "$progress / $total $progressLabel" else "$progress / $total"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusMd))
            .background(ProgressCardBg)
            .border(1.dp, ProgressCardTrack, RoundedCornerShape(AppDimens.radiusMd))
            .padding(horizontal = AppDimens.spaceLg, vertical = AppDimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceSm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = TextColor.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                color = TextColor.Secondary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(AppDimens.radiusPill))
                .background(ProgressCardTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(AppDimens.radiusPill))
                    .background(TrainingAccentTeal),
            )
        }
    }
}
