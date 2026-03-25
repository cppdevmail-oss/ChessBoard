package com.example.chessboard.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.chessboard.ui.theme.TrainingTextPrimary
import com.example.chessboard.ui.theme.TrainingTextSecondary

@Composable
fun ScreenTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextPrimary
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = color
    )
}

@Composable
fun SectionTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextPrimary
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = color
    )
}

@Composable
fun BodyPrimaryText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextPrimary
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

@Composable
fun BodySecondaryText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextSecondary
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextSecondary,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = fontWeight
    )
}

@Composable
fun CardTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TrainingTextPrimary
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = color
    )
}
