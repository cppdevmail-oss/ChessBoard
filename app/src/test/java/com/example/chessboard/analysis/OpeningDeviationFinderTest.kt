package com.example.chessboard.analysis

/**
 * Tests pure opening deviation detection without database or UI dependencies.
 * Keep screen behavior and persistence integration tests in their own packages.
 */
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.entity.GameEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpeningDeviationFinderTest {

    private val finder = OpeningDeviationFinder()

    @Test
    fun `findDeviations detects white move split from same position`() {
        val nf3Game = game(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val bc4Game = game(
            id = 2,
            pgn = storedPgn("e2e4", "e7e5", "f1c4"),
        )

        val deviations = finder.findDeviations(
            games = listOf(nf3Game, bc4Game),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(1, deviations.size)
        assertEquals(
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6",
            deviations.single().positionFen,
        )
        assertEquals(listOf(1L, 2L), deviations.single().games.map { game -> game.id })
    }

    @Test
    fun `findDeviations ignores opponent move split`() {
        val e5Game = game(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5"),
        )
        val c5Game = game(
            id = 2,
            pgn = storedPgn("e2e4", "c7c5"),
        )

        val deviations = finder.findDeviations(
            games = listOf(e5Game, c5Game),
            selectedSide = OpeningSide.WHITE,
        )

        assertTrue(deviations.isEmpty())
    }

    @Test
    fun `findDeviations detects black move split`() {
        val e5Game = game(
            id = 1,
            pgn = storedPgn("e2e4", "e7e5"),
        )
        val c5Game = game(
            id = 2,
            pgn = storedPgn("e2e4", "c7c5"),
        )

        val deviations = finder.findDeviations(
            games = listOf(e5Game, c5Game),
            selectedSide = OpeningSide.BLACK,
        )

        assertEquals(1, deviations.size)
        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3",
            deviations.single().positionFen,
        )
        assertEquals(listOf(1L, 2L), deviations.single().games.map { game -> game.id })
    }

    @Test
    fun `findDeviations treats repeated position in same game once`() {
        val repeatedGame = game(
            id = 1,
            pgn = storedPgn(
                "g1f3",
                "g8f6",
                "f3g1",
                "f6g8",
                "e2e4",
            ),
        )
        val nf3Game = game(
            id = 2,
            pgn = storedPgn("g1f3"),
        )

        val deviations = finder.findDeviations(
            games = listOf(repeatedGame, nf3Game),
            selectedSide = OpeningSide.WHITE,
        )

        assertTrue(deviations.isEmpty())
    }

    @Test
    fun `findDeviations keeps en passant target in position key`() {
        val e5Game = game(
            id = 1,
            pgn = storedPgn("e2e4", "d7d5", "e4e5"),
        )
        val exd5Game = game(
            id = 2,
            pgn = storedPgn("e2e4", "d7d5", "e4d5"),
        )

        val deviations = finder.findDeviations(
            games = listOf(e5Game, exd5Game),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(1, deviations.size)
        assertEquals(
            "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6",
            deviations.single().positionFen,
        )
    }

    @Test
    fun `findDeviations keeps unsaved games distinct by input position`() {
        val nf3Game = game(
            id = 0,
            pgn = storedPgn("e2e4", "e7e5", "g1f3"),
        )
        val bc4Game = game(
            id = 0,
            pgn = storedPgn("e2e4", "e7e5", "f1c4"),
        )

        val deviations = finder.findDeviations(
            games = listOf(nf3Game, bc4Game),
            selectedSide = OpeningSide.WHITE,
        )

        assertEquals(1, deviations.size)
        assertEquals(2, deviations.single().games.size)
    }

    private fun game(
        id: Long,
        pgn: String,
        initialFen: String = InitialBoardFen,
    ): GameEntity {
        return GameEntity(
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
}
