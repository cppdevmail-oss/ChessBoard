package com.example.chessboard.ui.components

/**
 * Shared board section wrapper for screens that need a standard interactive chess board.
 *
 * Keep generic board framing and sizing here. Do not add screen-specific controls,
 * training workflow logic, or persistence behavior to this file.
 */
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.ui.ChessBoardWithCoordinates
import com.example.chessboard.ui.theme.AppDimens

@Composable
fun ChessBoardSection(
    gameController: GameController,
    modifier: Modifier = Modifier,
    boardModifier: Modifier = Modifier,
) {
    // Pre-consume all user-input scroll so a parent LazyColumn never enters drag
    // state while the finger is on the board. Without this the LazyColumn's
    // DragGestureNode detaches mid-gesture and crashes (SIGSEGV).
    val noScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.UserInput) available else Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppDimens.radiusXl))
            .nestedScroll(noScroll)
    ) {
        ChessBoardWithCoordinates(
            gameController = gameController,
            modifier = boardModifier.fillMaxSize(),
        )
    }
}
