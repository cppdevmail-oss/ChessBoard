package com.example.chessboard.boardmodel

import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask

data class LineDraft(
    val line: LineEntity = LineEntity(
        // Room treats id = 0 as "not inserted yet" for auto-generated primary keys.
        id = 0,
        white = null,
        black = null,
        result = null,
        event = "",
        site = null,
        date = 0,
        round = null,
        eco = "",
        pgn = "",
        initialFen = "",
        sideMask = SideMask.WHITE,
    ),
)

fun buildLineDraftFromSourceLine(
    sourceLine: LineEntity,
): LineDraft {
    return LineDraft(
        line = sourceLine.copy(id = 0),
    )
}
