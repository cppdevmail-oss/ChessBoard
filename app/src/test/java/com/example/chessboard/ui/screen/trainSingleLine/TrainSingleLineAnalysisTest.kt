package com.example.chessboard.ui.screen.trainSingleLine

// Unit coverage for analysis-launch helpers used by the training screen.
// Keep pure state calculations here; UI navigation and database loading belong in instrumentation tests.

import com.example.chessboard.entity.LineEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainSingleLineAnalysisTest {

    @Test
    fun `resolveTrainingAnalysisInitialPly adds training range offset`() {
        val data = trainingLineData(
            uciMoves = listOf("g1f3", "b8c6"),
            analysisUciMoves = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
            analysisStartPly = 2,
        )

        val initialPly = resolveTrainingAnalysisInitialPly(
            trainingLineData = data,
            currentPly = 1,
        )

        assertEquals(3, initialPly)
    }

    @Test
    fun `resolveTrainingAnalysisInitialPly stays inside source line`() {
        val data = trainingLineData(
            uciMoves = listOf("g1f3"),
            analysisUciMoves = listOf("e2e4", "e7e5", "g1f3"),
            analysisStartPly = 2,
        )

        val initialPly = resolveTrainingAnalysisInitialPly(
            trainingLineData = data,
            currentPly = 10,
        )

        assertEquals(3, initialPly)
    }

    private fun trainingLineData(
        uciMoves: List<String>,
        analysisUciMoves: List<String>,
        analysisStartPly: Int,
    ): TrainSingleLineData {
        return TrainSingleLineData(
            line = LineEntity(
                id = 1L,
                event = "Test Line",
                pgn = "1. e4 e5 2. Nf3 Nc6 *",
                initialFen = "",
            ),
            uciMoves = uciMoves,
            analysisUciMoves = analysisUciMoves,
            analysisStartPly = analysisStartPly,
        )
    }
}
