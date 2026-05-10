package com.example.chessboard.service

import com.example.chessboard.repository.TrainingDao
import com.example.chessboard.repository.TrainingResultDao

data class SmartLinePair(val trainingId: Long, val lineId: Long)

class SmartTrainingService(
    private val trainingDao: TrainingDao,
    private val trainingResultDao: TrainingResultDao,
) {

    suspend fun countMasteredLines(lineIds: List<Long>): Int {
        return lineIds.count { lineId ->
            trainingResultDao.getResultsForLine(lineId, 1)
                .firstOrNull()?.mistakesCount == 0
        }
    }

    suspend fun resolveSmartQueue(
        selectedTrainingIds: Set<Long>,
        onlyWithMistakes: Boolean = false,
    ): List<SmartLinePair> {
        val allPairs = mutableListOf<SmartLinePair>()
        for (trainingId in selectedTrainingIds) {
            val training = trainingDao.getById(trainingId) ?: continue
            OneLineTrainingData.fromJson(training.linesJson).forEach { line ->
                allPairs.add(SmartLinePair(trainingId, line.lineId))
            }
        }
        if (allPairs.isEmpty()) return emptyList()

        val latestResults = allPairs.map { it.lineId }.distinct()
            .mapNotNull { lineId ->
                trainingResultDao.getResultsForLine(lineId, 1)
                    .firstOrNull()?.let { lineId to it }
            }.toMap()

        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
        val fiveDaysMs = 5 * 24 * 60 * 60 * 1000L

        val tier1 = mutableListOf<SmartLinePair>() // mistakes > 1
        val tier2 = mutableListOf<SmartLinePair>() // mistakes == 1, or no history
        val tier3a = mutableListOf<SmartLinePair>() // 0 mistakes, 3–5 days ago
        val tier3b = mutableListOf<SmartLinePair>() // 0 mistakes, 5+ days ago

        for (pair in allPairs) {
            val result = latestResults[pair.lineId]
            when {
                result == null -> tier2.add(pair)
                result.mistakesCount > 1 -> tier1.add(pair)
                result.mistakesCount == 1 -> tier2.add(pair)
                result.mistakesCount == 0 -> {
                    val elapsed = now - result.trainedAt
                    when {
                        elapsed >= fiveDaysMs -> tier3b.add(pair)
                        elapsed >= threeDaysMs -> tier3a.add(pair)
                    }
                }
            }
        }

        if (onlyWithMistakes) return tier1 + tier2
        val tier3 = tier3a.ifEmpty { tier3b }
        return tier1 + tier2 + tier3
    }
}
