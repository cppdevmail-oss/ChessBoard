package com.example.chessboard.service

import androidx.room.withTransaction
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.repository.AppDatabase
import com.github.bhlangonijr.chesslib.move.Move

class LineUpdater(
    private val database: AppDatabase
) {

    private val lineDao = database.lineDao()
    private val linePositionDao = database.linePositionDao()
    private val lineSaver = LineSaver(database)
    private val positionCleanupService = PositionCleanupService(database)

    /**
     * Replaces an existing line with the edited version while preserving uniqueness guarantees.
     *
     * The update flow is transactional:
     * 1. Remove links to all positions used by the edited line
     * 2. Delete or update affected positions depending on remaining usage
     * 3. Delete the old line row
     * 4. Save the edited line again through [LineSaver]
     *
     * If saving the edited line fails, the whole transaction is rolled back.
     */
    suspend fun updateLine(
        line: LineEntity,
        moves: List<Move>
    ): Boolean {
        return database.withTransaction {
            val affectedPositionIds = linePositionDao
                .getPositionsForLine(line.id)
                .map { it.positionId }
                .distinct()

            linePositionDao.deleteByLineId(line.id)
            positionCleanupService.cleanupPositions(affectedPositionIds)

            lineDao.deleteById(line.id)

            lineSaver.trySaveLine(
                line = line,
                moves = moves,
                sideMask = line.sideMask
            )
        }
    }
}
