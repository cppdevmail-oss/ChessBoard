package com.example.chessboard.service

import androidx.room.withTransaction
import com.example.chessboard.repository.AppDatabase

class LineDeleter(
    private val database: AppDatabase
) {

    private val lineDao = database.lineDao()
    private val linePositionDao = database.linePositionDao()
    private val positionCleanupService = PositionCleanupService(database)

    /**
     * Deletes a line and updates positions accordingly.
     *
     * - Removes position usage rows
     * - Removes the line
     * - Updates or deletes positions depending on remaining usage
     */
    suspend fun deleteLine(lineId: Long) {

        database.withTransaction {
            val affectedPositionIds = linePositionDao
                .getPositionsForLine(lineId)
                .map { it.positionId }
                .distinct()

            linePositionDao.deleteByLineId(lineId)
            lineDao.deleteById(lineId)
            positionCleanupService.cleanupPositions(affectedPositionIds)
        }
    }
}
