package com.example.chessboard.service

import android.util.Log
import androidx.room.withTransaction
import com.example.chessboard.repository.AppDatabase
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.LinePositionEntity
import com.example.chessboard.entity.PositionEntity
import com.example.chessboard.entity.SideMask
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move

class LineSaver(
    private val database: AppDatabase
) {
    private companion object {
        const val LogTag = "LineSaver"
    }

    private val lineDao = database.lineDao()
    private val positionDao = database.positionDao()
    private val linePositionDao = database.linePositionDao()
    private val uniquenessChecker = LineUniqueChecker(positionDao)

    /**
     * Attempts to save a chess line into the database.
     *
     * The line will be saved only if it contains at least one unique position
     * (i.e., a position that does not yet exist in the database).
     *
     * All operations are executed inside a single transaction to guarantee consistency.
     *
     * @param line Line metadata and PGN
     * @param moves List of moves to reconstruct the line
     * @param sideMask Side(s) for which this line/positions are relevant
     *
     * @return true if the line was successfully saved, false otherwise
     */
    suspend fun trySaveLine(
        line: LineEntity,
        moves: List<Move>,
        sideMask: Int
    ): Boolean {
        return saveLine(line, moves, sideMask) != null
    }

    suspend fun saveOrGetExistingLineId(
        line: LineEntity,
        moves: List<Move>,
        sideMask: Int
    ): Long? {
        val savedId = saveLine(line, moves, sideMask)
        if (savedId != null) return savedId

        val board = Board()
        if (line.initialFen.isNotEmpty()) board.loadFromFen(line.initialFen)
        for (move in moves) board.doMove(move)

        val position = positionDao.getIdAndSideByHashAndFen(board.zobristKey, board.fen)
            ?: return null
        return linePositionDao.getLineIdByPositionAndPly(position.id, moves.size)
    }

    suspend fun saveLine(
        line: LineEntity,
        moves: List<Move>,
        sideMask: Int
    ): Long? {

        return database.withTransaction {

            val isUnique = uniquenessChecker.hasUniquePosition(
                line.initialFen,
                moves, sideMask
            )

            if (!isUnique) {
                return@withTransaction null
            }

            val lineId = lineDao.insertLine(line)

            if (lineId == -1L) {
                return@withTransaction null
            }

            val board = Board()

            if (line.initialFen.isNotEmpty()) {
                board.loadFromFen(line.initialFen)
            }

            var ply = 0
            savePositionAndLink(lineId, board, ply, sideMask)
            for (move in moves) {
                board.doMove(move)
                ply++
                savePositionAndLink(lineId, board, ply, sideMask)
            }

            lineId
        }
    }

    /**
     * Saves the current board position (if needed) and creates a link
     * between the line and the position.
     *
     * @param lineId ID of the line
     * @param board Current board state
     * @param ply Move index (half-move number)
     * @param sideMask Side(s) for which this position is relevant
     */
    private suspend fun savePositionAndLink(
        lineId: Long,
        board: Board,
        ply: Int,
        sideMask: Int
    ) {
        val normalizedFen = normalizeFenWithoutMoveNumbers(board.fen)
        val hashNoMoveNumber = calculateFenHashWithoutMoveNumbers(board.fen)
        Log.d(
            LogTag,
            "savePositionAndLink lineId=$lineId ply=$ply sideMask=$sideMask " +
                "fen=${board.fen} normalizedFen=$normalizedFen " +
                "hashNoMoveNumber=$hashNoMoveNumber"
        )
        val positionId = getOrInsertPositionId(
            hash = board.zobristKey,
            hashNoMoveNumber = hashNoMoveNumber,
            fen = board.fen,
            sideMask = sideMask
        )
        linePositionDao.insertLinePosition(
            LinePositionEntity(
                lineId = lineId,
                positionId = positionId,
                ply = ply,
                sideMask = sideMask,
            )
        )
    }

    /**
     * Retrieves an existing position ID by (hash + FEN),
     * or inserts a new position if it does not exist.
     *
     * If the position already exists but was previously stored for a different side,
     * the sideMask is updated to include both sides.
     *
     * @param hash Zobrist hash of the position
     * @param fen Full FEN string of the position
     * @param sideMask Side(s) for which this position is currently being stored
     *
     * @return ID of the existing or newly inserted position
     */
    private suspend fun getOrInsertPositionId(
        hash: Long,
        hashNoMoveNumber: Long,
        fen: String,
        sideMask: Int
    ): Long {
        val existingIdAndSide = positionDao.getIdAndSideByHashAndFen(hash, fen)

        if (existingIdAndSide == null) {
            val posEntity = PositionEntity(
                hash = hash,
                hashNoMoveNumber = hashNoMoveNumber,
                fen = fen,
                sideMask = sideMask
            )
            return positionDao.insertPosition(posEntity)
        }

        if (existingIdAndSide.sideMask != sideMask) {
            positionDao.updateSideMask(existingIdAndSide.id, SideMask.BOTH)
        }

        return existingIdAndSide.id
    }
}
