package com.example.chessboard.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessboard.ui.components.AppSettingsScaffold
import com.example.chessboard.ui.components.AppSettingsToggleRow
import com.example.chessboard.ui.components.CardSurface
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.ChessBoardTheme
import com.example.chessboard.ui.theme.TextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreenContainer(
    screenContext: ScreenContainerContext,
    simpleViewEnabled: Boolean,
    onSimpleViewToggle: (Boolean) -> Unit,
    removeLineIfRepIsZero: Boolean,
    onRemoveLineIfRepIsZeroToggle: (Boolean) -> Unit,
    hideLinesWithWeightZero: Boolean,
    onHideLinesWithWeightZeroToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userProfileService = remember(screenContext.inDbProvider) {
        screenContext.inDbProvider.createUserProfileService()
    }
    val scope = rememberCoroutineScope()
    var autoNextLine by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val profile = withContext(Dispatchers.IO) { userProfileService.getProfile() }
        autoNextLine = profile.autoNextLine
    }

    SettingsScreen(
        simpleViewEnabled = simpleViewEnabled,
        onSimpleViewToggle = onSimpleViewToggle,
        removeLineIfRepIsZero = removeLineIfRepIsZero,
        onRemoveLineIfRepIsZeroToggle = onRemoveLineIfRepIsZeroToggle,
        hideLinesWithWeightZero = hideLinesWithWeightZero,
        onHideLinesWithWeightZeroToggle = onHideLinesWithWeightZeroToggle,
        autoNextLine = autoNextLine,
        onAutoNextLineChange = { newValue ->
            autoNextLine = newValue
            scope.launch(Dispatchers.IO) { userProfileService.updateAutoNextLine(newValue) }
        },
        onBackClick = screenContext.onBackClick,
        onNavigate = screenContext.onNavigate,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    simpleViewEnabled: Boolean,
    onSimpleViewToggle: (Boolean) -> Unit,
    removeLineIfRepIsZero: Boolean,
    onRemoveLineIfRepIsZeroToggle: (Boolean) -> Unit,
    hideLinesWithWeightZero: Boolean,
    onHideLinesWithWeightZeroToggle: (Boolean) -> Unit,
    autoNextLine: Boolean = false,
    onAutoNextLineChange: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppSettingsScaffold(
        title = "Settings",
        subtitle = "Customize your experience",
        selectedNavItem = ScreenType.Settings,
        onBackClick = onBackClick,
        onNavigate = onNavigate,
        filledBackButton = true,
        modifier = modifier,
    ) {
        DisplaySection(
            simpleViewEnabled = simpleViewEnabled,
            onSimpleViewToggle = onSimpleViewToggle,
        )
        TrainingSection(
            removeLineIfRepIsZero = removeLineIfRepIsZero,
            onRemoveLineIfRepIsZeroToggle = onRemoveLineIfRepIsZeroToggle,
            hideLinesWithWeightZero = hideLinesWithWeightZero,
            onHideLinesWithWeightZeroToggle = onHideLinesWithWeightZeroToggle,
            autoNextLine = autoNextLine,
            onAutoNextLineChange = onAutoNextLineChange,
        )
    }
}

@Composable
private fun DisplaySection(
    simpleViewEnabled: Boolean,
    onSimpleViewToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CardSurface(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = "DISPLAY",
            modifier = Modifier.padding(
                start = AppDimens.spaceLg,
                top = AppDimens.spaceLg,
                end = AppDimens.spaceLg,
                bottom = AppDimens.spaceMd,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = TextColor.Secondary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        SimpleViewRow(
            enabled = simpleViewEnabled,
            onToggle = onSimpleViewToggle,
        )
    }
}

@Composable
private fun TrainingSection(
    removeLineIfRepIsZero: Boolean,
    onRemoveLineIfRepIsZeroToggle: (Boolean) -> Unit,
    hideLinesWithWeightZero: Boolean,
    onHideLinesWithWeightZeroToggle: (Boolean) -> Unit,
    autoNextLine: Boolean,
    onAutoNextLineChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CardSurface(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = "TRAINING",
            modifier = Modifier.padding(
                start = AppDimens.spaceLg,
                top = AppDimens.spaceLg,
                end = AppDimens.spaceLg,
                bottom = AppDimens.spaceMd,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = TextColor.Secondary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        AppSettingsToggleRow(
            icon = Icons.Filled.FitnessCenter,
            title = "Remove line if rep is 0",
            subtitle = "Remove lines from training when repetitions reach zero",
            checked = removeLineIfRepIsZero,
            onCheckedChange = onRemoveLineIfRepIsZeroToggle,
        )
        AppSettingsToggleRow(
            icon = Icons.Filled.VisibilityOff,
            title = "Hide lines with weight 0",
            subtitle = "Hide exhausted lines from the training editor",
            checked = hideLinesWithWeightZero,
            onCheckedChange = onHideLinesWithWeightZeroToggle,
        )
        AppSettingsToggleRow(
            icon = Icons.Filled.FastForward,
            title = "Auto Next Line",
            subtitle = "Skip completion dialog and move to the next line",
            checked = autoNextLine,
            onCheckedChange = onAutoNextLineChange,
        )
    }
}

@Composable
private fun SimpleViewRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSettingsToggleRow(
        icon = Icons.Filled.Visibility,
        title = "Simple View",
        subtitle = "Simplified interface with minimal distractions",
        checked = enabled,
        onCheckedChange = onToggle,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ChessBoardTheme {
        SettingsScreen(
            simpleViewEnabled = false,
            onSimpleViewToggle = {},
            removeLineIfRepIsZero = false,
            onRemoveLineIfRepIsZeroToggle = {},
            hideLinesWithWeightZero = false,
            onHideLinesWithWeightZeroToggle = {},
        )
    }
}
