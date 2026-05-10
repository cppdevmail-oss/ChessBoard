package com.example.chessboard.analysis

/**
 * Builds UI-facing opening deviation items from pure deviation results.
 * Keep branch aggregation and one-ply replay logic here.
 * Do not add screen wiring, navigation, or database queries to this file.
 */
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.service.parsePgnMoves
import com.example.chessboard.ui.screen.openingDeviation.OpeningDeviationBranch
import com.example.chessboard.ui.screen.openingDeviation.OpeningDeviationItem

class OpeningDeviationItemBuilder(
    private val finder: OpeningDeviationFinder = OpeningDeviationFinder(),
) {

    fun build(
        lines: List<LineEntity>,
        selectedSide: OpeningSide,
    ): List<OpeningDeviationItem> {
        return finder.findDeviations(
            lines = lines,
            selectedSide = selectedSide,
        ).map { deviation ->
            OpeningDeviationItem(
                positionFen = deviation.positionFen,
                branches = buildBranches(
                    deviation = deviation,
                    selectedSide = selectedSide,
                ),
            )
        }
    }

    private fun buildBranches(
        deviation: OpeningDeviation,
        selectedSide: OpeningSide,
    ): List<OpeningDeviationBranch> {
        val branchesByResultFen = linkedMapOf<String, BranchBucket>()

        deviation.lines.forEach { line ->
            recordLineBranch(
                line = line,
                deviationPositionFen = deviation.positionFen,
                selectedSide = selectedSide,
                branchesByResultFen = branchesByResultFen,
            )
        }

        return branchesByResultFen.values.map { bucket ->
            OpeningDeviationBranch(
                moveUci = bucket.moveUci,
                resultFen = bucket.resultFen,
                linesCount = bucket.linesCount,
            )
        }
    }

    private fun recordLineBranch(
        line: LineEntity,
        deviationPositionFen: String,
        selectedSide: OpeningSide,
        branchesByResultFen: MutableMap<String, BranchBucket>,
    ) {
        val board = OpeningDeviationReplay.buildInitialBoard(line.initialFen)
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
                !OpeningDeviationReplay.isSelectedSideToMove(board, selectedSide) ||
                positionKey != deviationPositionFen
            ) {
                board.doMove(move)
                continue
            }

            board.doMove(move)
            recordBranch(
                moveUci = uciMove.lowercase(),
                resultFen = OpeningDeviationReplay.buildPositionKey(board),
                branchesByResultFen = branchesByResultFen,
            )
            return
        }
    }

    private fun recordBranch(
        moveUci: String,
        resultFen: String,
        branchesByResultFen: MutableMap<String, BranchBucket>,
    ) {
        val existingBucket = branchesByResultFen[resultFen]
        if (existingBucket != null) {
            existingBucket.linesCount += 1
            return
        }

        branchesByResultFen[resultFen] = BranchBucket(
            moveUci = moveUci,
            resultFen = resultFen,
            linesCount = 1,
        )
    }

    private data class BranchBucket(
        val moveUci: String,
        val resultFen: String,
        var linesCount: Int,
    )
}
