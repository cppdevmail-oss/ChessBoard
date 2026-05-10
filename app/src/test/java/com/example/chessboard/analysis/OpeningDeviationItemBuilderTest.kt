package com.example.chessboard.analysis

/**
 * Tests pure building of UI-facing opening deviation items.
 * Keep Android UI rendering and navigation coverage out of this file.
 */
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.entity.LineEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpeningDeviationItemBuilderTest {

    private val builder = OpeningDeviationItemBuilder()

    @Test
    fun `build creates one start position with two branches`() {
        val nf3Line = line(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val bc4Line = line(
            id = 2,
            pgn = storedPgn("e2e4", "e7e5", "f1c4"),
        )

        val items = builder.build(
            lines = listOf(nf3Line, bc4Line),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(1, items.size)
        assertEquals(
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6",
            items.single().positionFen,
        )
        assertEquals(
            listOf(
                branch(
                    moveUci = "g1f3",
                    resultFen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq -",
                    linesCount = 1,
                ),
                branch(
                    moveUci = "f1c4",
                    resultFen = "rnbqkbnr/pppp1ppp/8/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR b KQkq -",
                    linesCount = 1,
                ),
            ),
            items.single().branches,
        )
    }

    @Test
    fun `build creates multiple deviation start positions`() {
        val nf3Line = line(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val bc4Line = line(
            id = 2,
            pgn = storedPgn("e2e4", "e7e5", "f1c4"),
        )
        val c4Line = line(
            id = 3,
            pgn = storedPgn("d2d4", "d7d5", "c2c4"),
        )
        val g3Line = line(
            id = 4,
            pgn = storedPgn("d2d4", "d7d5", "g2g3"),
        )

        val items = builder.build(
            lines = listOf(nf3Line, bc4Line, c4Line, g3Line),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(3, items.size)
        assertEquals(
            listOf(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6",
                "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq d6",
            ),
            items.map { item -> item.positionFen },
        )
    }

    @Test
    fun `build returns empty list when no deviations exist`() {
        val firstLine = line(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val secondLine = line(
            id = 2,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )

        val items = builder.build(
            lines = listOf(firstLine, secondLine),
            selectedSide = OpeningSide.WHITE,
        )

        assertTrue(items.isEmpty())
    }

    @Test
    fun `build merges repeated final positions into one branch with lines count`() {
        val firstNf3Line = line(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val secondNf3Line = line(
            id = 2,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val bc4Line = line(
            id = 3,
            pgn = storedPgn("e2e4", "e7e5", "f1c4"),
        )

        val items = builder.build(
            lines = listOf(firstNf3Line, secondNf3Line, bc4Line),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(1, items.size)
        assertEquals(2, items.single().branches.size)
        assertEquals(
            listOf(2, 1),
            items.single().branches.map { branch -> branch.linesCount },
        )
        assertEquals(
            listOf("g1f3", "f1c4"),
            items.single().branches.map { branch -> branch.moveUci },
        )
    }

    private fun line(
        id: Long,
        pgn: String,
        initialFen: String = InitialBoardFen,
    ): LineEntity {
        return LineEntity(
            id = id,
            pgn = pgn,
            initialFen = initialFen,
        )
    }

    private fun storedPgn(vararg moves: String): String {
        return buildString {
            append("[Event \"Test\"]\n")
            append("[White \"White\"]\n")
            append("[Black \"Black\"]\n")
            append("[Result \"*\"]\n\n")

            moves.forEachIndexed { index, move ->
                if (index % 2 == 0) {
                    append("${index / 2 + 1}. ")
                }
                append(move)
                append(" ")
            }

            append("*")
        }
    }

    private fun branch(
        moveUci: String,
        resultFen: String,
        linesCount: Int,
    ) = com.example.chessboard.ui.screen.openingDeviation.OpeningDeviationBranch(
        moveUci = moveUci,
        resultFen = resultFen,
        linesCount = linesCount,
    )
}
