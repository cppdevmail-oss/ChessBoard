package com.example.chessboard.service

import com.example.chessboard.entity.PositionEntity
import com.example.chessboard.repository.AppDatabase

class PositionService(
    private val database: AppDatabase
) {

    suspend fun findPositionsByFenWithoutMoveNumber(fen: String): List<PositionEntity> {
        val normalizedFen = normalizeFenWithoutMoveNumbers(fen)
        val hashNoMoveNumber = calculateFenHashWithoutMoveNumbers(normalizedFen)
        val candidates = database.positionDao().getPositionsByHashNoMoveNumber(hashNoMoveNumber)

        return candidates.filter { position ->
            normalizeFenWithoutMoveNumbers(position.fen) == normalizedFen
        }
    }

    suspend fun findGameIdsByFenWithoutMoveNumber(fen: String): List<Long> {
        val matchingPositions = findPositionsByFenWithoutMoveNumber(fen)
        val positionIds = matchingPositions.map { position -> position.id }

        if (positionIds.isEmpty()) {
            return emptyList()
        }

        return database.gamePositionDao().getGameIdsByPositionIds(positionIds)
    }
}
