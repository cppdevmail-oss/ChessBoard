package com.example.chessboard.ui.screen

import androidx.compose.ui.graphics.Color
import com.example.chessboard.entity.SideMask
import com.example.chessboard.ui.BoardOrientation

internal val SideButtonSelectedBg = Color(0xFF2C2C2C)

enum class EditableLineSide(
    val sideMask: Int,
    val orientation: BoardOrientation,
) {
    AS_WHITE(
        sideMask = SideMask.WHITE,
        orientation = BoardOrientation.WHITE,
    ),
    AS_BLACK(
        sideMask = SideMask.BLACK,
        orientation = BoardOrientation.BLACK,
    );

    companion object {
        fun fromSideMask(sideMask: Int): EditableLineSide {
            if (sideMask == SideMask.BLACK) {
                return AS_BLACK
            }

            return AS_WHITE
        }
    }
}
