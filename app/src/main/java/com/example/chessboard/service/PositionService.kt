package com.example.chessboard.service

import android.util.Log
import com.example.chessboard.entity.PositionEntity
import com.example.chessboard.repository.AppDatabase

class PositionService(
    private val database: AppDatabase
) {
    private companion object {
        const val LogTag = "PositionService"
    }

    suspend fun findPositionsByFenWithoutMoveNumber(fen: String): List<PositionEntity> {
        val normalizedFen = normalizeFenWithoutMoveNumbers(fen)
        val hashNoMoveNumber = calculateFenHashWithoutMoveNumbers(normalizedFen)
        Log.d(
            LogTag,
            "findPositionsByFenWithoutMoveNumber fen=$normalizedFen hash=$hashNoMoveNumber"
        )
        val candidates = database.positionDao().getPositionsByHashNoMoveNumber(hashNoMoveNumber)

        return candidates.filter { position ->
            normalizeFenWithoutMoveNumbers(position.fen) == normalizedFen
        }
    }

    suspend fun findLineIdsByFenWithoutMoveNumber(fen: String): List<Long> {
        val matchingPositions = findPositionsByFenWithoutMoveNumber(fen)
        val positionIds = matchingPositions.map { position -> position.id }

        if (positionIds.isEmpty()) {
            return emptyList()
        }

        return database.linePositionDao().getLineIdsByPositionIds(positionIds)
    }
}
