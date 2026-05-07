package com.example.chessboard.ui.screen.positions.positionSearch

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessboard.ui.components.AppSettingsScaffold
import com.example.chessboard.ui.components.CardSurface
import com.example.chessboard.ui.screen.PositionSearchCastlesSection
import com.example.chessboard.ui.screen.PositionSearchCastlingState
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.replacePositionSearchFenCastlingPart
import com.example.chessboard.ui.screen.resolvePositionSearchCastlingState
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TextColor

@Composable
fun PositionSearchSettingsScreenContainer(
    currentFen: String,
    onFenChange: (String) -> Unit,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier
) {
    val castlingState = resolvePositionSearchCastlingState(currentFen)

    PositionSearchSettingsScreen(
        castlingState = castlingState,
        onCastlingStateChange = { newState ->
            onFenChange(replacePositionSearchFenCastlingPart(currentFen, newState))
        },
        onBackClick = screenContext.onBackClick,
        onNavigate = screenContext.onNavigate,
        modifier = modifier
    )
}

@Composable
private fun PositionSearchSettingsScreen(
    castlingState: PositionSearchCastlingState,
    onCastlingStateChange: (PositionSearchCastlingState) -> Unit,
    onBackClick: () -> Unit,
    onNavigate: (ScreenType) -> Unit,
    modifier: Modifier = Modifier
) {
    AppSettingsScaffold(
        title = "Position Search Settings",
        selectedNavItem = ScreenType.PositionSearch,
        onBackClick = onBackClick,
        onNavigate = onNavigate,
        modifier = modifier,
    ) {
        CardSurface(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = "CASTLING",
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
            PositionSearchCastlesSection(
                castlingState = castlingState,
                onCastlingStateChange = onCastlingStateChange,
                showTitle = false,
                modifier = Modifier.padding(bottom = AppDimens.spaceMd),
            )
        }
    }
}
