package com.example.chessboard.analysis

/**
 * Contains pure opening-line analysis that works with loaded line records.
 * Keep database access, UI state, and screen workflow code outside this file.
 */
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.service.parsePgnMoves

enum class OpeningSide {
    WHITE,
    BLACK,
}

data class OpeningDeviation(
    val positionFen: String,
    val lines: List<LineEntity>,
)

class OpeningDeviationFinder {

    /**
     * Finds opening-line deviations for one selected side.
     *
     * Algorithm:
     * 1. Iterate over every input line and replay its stored UCI moves on a chesslib [Board].
     * 2. Before each move, inspect the current board position. A position is considered only when
     *    the side to move is [selectedSide]; opponent turns are only replayed to advance the board.
     * 3. Convert the current board to a comparison key using FEN without halfmove/fullmove counters,
     *    while keeping piece placement, side to move, castling rights, and en passant target.
     * 4. Track position keys already seen inside the current line. If the same position appears
     *    again in that line, ignore the repeated occurrence so one line contributes only once per
     *    position.
     * 5. For the first occurrence of a selected-side position in a line, record two facts in the
     *    bucket for that position:
     *    - the line that reached this position;
     *    - the next move played by the selected side from this position.
     * 6. After all lines are scanned, a position is a deviation when its bucket contains more than
     *    one unique next move. The public result returns the position FEN and all lines that reached
     *    that position; the move set is only an internal detail used to detect the split.
     *
     * Lines with a persisted id are deduplicated by that id. Lines with id = 0 are treated as
     * distinct input items by their index, which keeps multiple unsaved lines separate.
     */
    fun findDeviations(
        lines: List<LineEntity>,
        selectedSide: OpeningSide,
    ): List<OpeningDeviation> {
        val bucketsByPosition = linkedMapOf<String, PositionBucket>()

        lines.forEachIndexed { lineIndex, line ->
            scanLine(
                line = line,
                lineIndex = lineIndex,
                selectedSide = selectedSide,
                bucketsByPosition = bucketsByPosition,
            )
        }

        return bucketsByPosition.values
            .filter { bucket -> bucket.nextMoves.size > 1 }
            .map { bucket ->
                OpeningDeviation(
                    positionFen = bucket.positionFen,
                    lines = bucket.lines.values.toList(),
                )
            }
    }

    private fun scanLine(
        line: LineEntity,
        lineIndex: Int,
        selectedSide: OpeningSide,
        bucketsByPosition: MutableMap<String, PositionBucket>,
    ) {
        val board = OpeningDeviationReplay.buildInitialBoard(line.initialFen)
        val seenPositionsInLine = mutableSetOf<String>()
        val moves = parsePgnMoves(line.pgn)

        for ((moveIndex, uciMove) in moves.withIndex()) {
            val positionKey = OpeningDeviationReplay.buildPositionKey(board)
            val move = OpeningDeviationReplay.buildMoveFromUci(
                uci = uciMove,
                board = board,
                line = line,
                moveIndex = moveIndex,
            )

            if (
                OpeningDeviationReplay.isSelectedSideToMove(board, selectedSide) &&
                seenPositionsInLine.add(positionKey)
            ) {
                recordPosition(
                    positionKey = positionKey,
                    line = line,
                    lineIndex = lineIndex,
                    nextMove = uciMove.lowercase(),
                    bucketsByPosition = bucketsByPosition,
                )
            }

            board.doMove(move)
        }
    }

    private fun recordPosition(
        positionKey: String,
        line: LineEntity,
        lineIndex: Int,
        nextMove: String,
        bucketsByPosition: MutableMap<String, PositionBucket>,
    ) {
        val bucket = bucketsByPosition.getOrPut(positionKey) {
            PositionBucket(positionFen = positionKey)
        }

        bucket.nextMoves.add(nextMove)
        bucket.lines[LineKey.from(line = line, lineIndex = lineIndex)] = line
    }

    private data class PositionBucket(
        val positionFen: String,
        val lines: LinkedHashMap<LineKey, LineEntity> = linkedMapOf(),
        val nextMoves: MutableSet<String> = linkedSetOf(),
    )

    private data class LineKey(
        val stableId: Long?,
        val inputIndex: Int?,
    ) {
        companion object {
            fun from(line: LineEntity, lineIndex: Int): LineKey {
                if (line.id != 0L) {
                    return LineKey(stableId = line.id, inputIndex = null)
                }

                return LineKey(stableId = null, inputIndex = lineIndex)
            }
        }
    }
}
