package com.example.chessboard.boardmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class LineVariationLineStateTest {

    @Test
    fun `recordPlayedPath creates first line`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))

        assertEquals(
            listOf(listOf("e2e4", "e7e5", "g1f3")),
            state.lines,
        )
        assertEquals(listOf("e2e4", "e7e5", "g1f3"), state.currentPath)
    }

    @Test
    fun `selectPath does not remove existing line`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))
            .selectPath(listOf("e2e4"))

        assertEquals(
            listOf(listOf("e2e4", "e7e5", "g1f3")),
            state.lines,
        )
        assertEquals(listOf("e2e4"), state.currentPath)
    }

    @Test
    fun `recordPlayedPath creates variation from middle`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))
            .selectPath(listOf("e2e4", "e7e5"))
            .recordPlayedPath(listOf("e2e4", "e7e5", "d2d4"))

        assertEquals(
            listOf(
                listOf("e2e4", "e7e5", "g1f3"),
                listOf("e2e4", "e7e5", "d2d4"),
            ),
            state.lines,
        )
        assertEquals(listOf("e2e4", "e7e5", "d2d4"), state.currentPath)
    }

    @Test
    fun `recordPlayedPath does not duplicate same line`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))

        assertEquals(
            listOf(listOf("e2e4", "e7e5", "g1f3")),
            state.lines,
        )
    }

    @Test
    fun `recordPlayedPath does not add shorter prefix when full line exists`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3"))

        assertEquals(
            listOf(listOf("e2e4", "e7e5", "g1f3", "b8c6")),
            state.lines,
        )
        assertEquals(listOf("e2e4", "e7e5", "g1f3"), state.currentPath)
    }

    @Test
    fun `backingLineFor returns full line for selected prefix`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
            .selectPath(listOf("e2e4", "e7e5", "g1f3"))

        assertEquals(
            listOf("e2e4", "e7e5", "g1f3", "b8c6"),
            state.backingLineFor(),
        )
    }

    @Test
    fun `recordPlayedPath normalizes move casing and whitespace`() {
        val state = LineVariationLineState()
            .recordPlayedPath(listOf(" E2E4 ", "e7e5", ""))

        assertEquals(
            listOf(listOf("e2e4", "e7e5")),
            state.lines,
        )
        assertEquals(listOf("e2e4", "e7e5"), state.currentPath)
    }
}
