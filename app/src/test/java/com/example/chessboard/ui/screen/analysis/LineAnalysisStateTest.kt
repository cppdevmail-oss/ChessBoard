package com.example.chessboard.ui.screen.analysis

import com.example.chessboard.boardmodel.LineController
import com.example.chessboard.boardmodel.LineVariationLineState
import org.junit.Assert.assertEquals
import org.junit.Test

class LineAnalysisStateTest {

    @Test
    fun `resolveNextVariationState records played move`() {
        val lineController = LineController()

        lineController.tryMove("e2", "e4")
        val state = resolveNextVariationState(
            variationState = LineVariationLineState(),
            lineController = lineController,
        )

        assertEquals(listOf(listOf("e2e4")), state.lines)
        assertEquals(listOf("e2e4"), state.currentPath)
    }

    @Test
    fun `resolveNextVariationState keeps line after undo`() {
        val lineController = LineController()
        lineController.tryMove("e2", "e4")
        lineController.undoMove()

        val state = resolveNextVariationState(
            variationState = LineVariationLineState(),
            lineController = lineController,
        )

        assertEquals(listOf(listOf("e2e4")), state.lines)
        assertEquals(emptyList<String>(), state.currentPath)
    }

    @Test
    fun `resolveNextVariationState creates branch from middle`() {
        val lineController = LineController()
        lineController.tryMove("e2", "e4")
        lineController.tryMove("e7", "e5")
        lineController.tryMove("g1", "f3")
        val firstLineState = resolveNextVariationState(
            variationState = LineVariationLineState(),
            lineController = lineController,
        )

        lineController.undoMove()
        lineController.tryMove("d2", "d4")
        val branchedState = resolveNextVariationState(
            variationState = firstLineState,
            lineController = lineController,
        )

        assertEquals(
            listOf(
                listOf("e2e4", "e7e5", "g1f3"),
                listOf("e2e4", "e7e5", "d2d4"),
            ),
            branchedState.lines,
        )
        assertEquals(listOf("e2e4", "e7e5", "d2d4"), branchedState.currentPath)
    }
}
