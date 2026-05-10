package com.example.chessboard.boardmodel

import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import org.junit.Assert.assertEquals
import org.junit.Test

class LineDraftTest {

    @Test
    fun buildLineDraftFromSourceLine_clonesLineAndResetsId() {
        val sourceLine = LineEntity(
            id = 42L,
            white = "Carlsen",
            black = "Nepo",
            result = "1-0",
            event = "World Championship",
            site = "Dubai",
            date = 1700000000000L,
            round = "6",
            eco = "C88",
            pgn = "1. e4 e5 2. Nf3 Nc6 *",
            initialFen = "",
            sideMask = SideMask.BLACK
        )

        val draft = buildLineDraftFromSourceLine(sourceLine)

        assertEquals(0L, draft.line.id)
        assertEquals(sourceLine.white, draft.line.white)
        assertEquals(sourceLine.black, draft.line.black)
        assertEquals(sourceLine.result, draft.line.result)
        assertEquals(sourceLine.event, draft.line.event)
        assertEquals(sourceLine.site, draft.line.site)
        assertEquals(sourceLine.date, draft.line.date)
        assertEquals(sourceLine.round, draft.line.round)
        assertEquals(sourceLine.eco, draft.line.eco)
        assertEquals(sourceLine.pgn, draft.line.pgn)
        assertEquals(sourceLine.initialFen, draft.line.initialFen)
        assertEquals(sourceLine.sideMask, draft.line.sideMask)
    }
}
