package com.example.chessboard.service

import com.example.chessboard.entity.TrainingEntity
import com.example.chessboard.entity.TrainingResultEntity
import com.example.chessboard.repository.TrainingDao
import com.example.chessboard.repository.TrainingResultDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartTrainingServiceTest {

    private val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
    private val fiveDaysMs = 5 * 24 * 60 * 60 * 1000L
    private val now = System.currentTimeMillis()

    // region helpers

    private fun training(id: Long, vararg gameIds: Long): TrainingEntity {
        val json = OneGameTrainingData.toJson(gameIds.map { OneGameTrainingData(it, 1) })
        return TrainingEntity(id = id, name = "T$id", gamesJson = json)
    }

    private fun result(gameId: Long, mistakes: Int, agoMs: Long = 0L): TrainingResultEntity =
        TrainingResultEntity(gameId = gameId, mistakesCount = mistakes, trainedAt = now - agoMs)

    private fun service(
        trainings: List<TrainingEntity>,
        results: List<TrainingResultEntity> = emptyList(),
    ): SmartTrainingService {
        return SmartTrainingService(
            FakeTrainingDao(trainings),
            FakeTrainingResultDao(results),
        )
    }

    // endregion

    @Test
    fun `returns empty list when no trainings selected`() = runBlocking {
        val svc = service(trainings = listOf(training(1L, 10L)))
        val queue = svc.resolveSmartQueue(emptySet())
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `returns empty list when selected training does not exist`() = runBlocking {
        val svc = service(trainings = emptyList())
        val queue = svc.resolveSmartQueue(setOf(99L))
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `game with no history goes to tier2`() = runBlocking {
        val svc = service(trainings = listOf(training(1L, 10L)), results = emptyList())
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `game with more than 1 mistake goes to tier1 before tier2`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L, 20L)),
            results = listOf(
                result(10L, mistakes = 2),
                result(20L, mistakes = 0),
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        // game 20 has 0 mistakes and was just trained — not in any tier
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `game with exactly 1 mistake goes to tier2`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L)),
            results = listOf(result(10L, mistakes = 1)),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `tier1 games come before tier2 games`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L, 20L)),
            results = listOf(
                result(10L, mistakes = 1),
                result(20L, mistakes = 3),
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(SmartGamePair(1L, 20L), queue[0])
        assertEquals(SmartGamePair(1L, 10L), queue[1])
    }

    @Test
    fun `game with 0 mistakes trained 3 to 5 days ago goes to tier3a`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L)),
            results = listOf(result(10L, mistakes = 0, agoMs = threeDaysMs + 1000L)),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `game with 0 mistakes trained more than 5 days ago goes to tier3b`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L)),
            results = listOf(result(10L, mistakes = 0, agoMs = fiveDaysMs + 1000L)),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `tier3b used as fallback when tier3a is empty`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L, 20L)),
            results = listOf(
                result(10L, mistakes = 0, agoMs = fiveDaysMs + 1000L),
                result(20L, mistakes = 0, agoMs = fiveDaysMs + 2000L),
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertEquals(2, queue.size)
        assertTrue(queue.all { it.trainingId == 1L })
    }

    @Test
    fun `tier3a used when both tier3a and tier3b have games`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L, 20L)),
            results = listOf(
                result(10L, mistakes = 0, agoMs = threeDaysMs + 1000L), // 3a
                result(20L, mistakes = 0, agoMs = fiveDaysMs + 1000L),  // 3b
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        // only tier3a selected when non-empty
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    @Test
    fun `game with 0 mistakes trained less than 3 days ago not included`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L)),
            results = listOf(result(10L, mistakes = 0, agoMs = threeDaysMs - 1000L)),
        )
        val queue = svc.resolveSmartQueue(setOf(1L))
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `games are collected from multiple selected trainings`() = runBlocking {
        val svc = service(
            trainings = listOf(
                training(1L, 10L),
                training(2L, 20L),
            ),
            results = listOf(
                result(10L, mistakes = 1),
                result(20L, mistakes = 2),
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L, 2L))
        assertEquals(2, queue.size)
        assertTrue(queue.any { it.trainingId == 1L && it.gameId == 10L })
        assertTrue(queue.any { it.trainingId == 2L && it.gameId == 20L })
    }

    @Test
    fun `onlyWithMistakes excludes tier3 games`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L, 20L, 30L)),
            results = listOf(
                result(10L, mistakes = 2),
                result(20L, mistakes = 1),
                result(30L, mistakes = 0, agoMs = fiveDaysMs + 1000L),
            ),
        )
        val queue = svc.resolveSmartQueue(setOf(1L), onlyWithMistakes = true)
        assertTrue(queue.any { it.gameId == 10L })
        assertTrue(queue.any { it.gameId == 20L })
        assertTrue(queue.none { it.gameId == 30L })
    }

    @Test
    fun `onlyWithMistakes false includes tier3 games`() = runBlocking {
        val svc = service(
            trainings = listOf(training(1L, 10L)),
            results = listOf(result(10L, mistakes = 0, agoMs = fiveDaysMs + 1000L)),
        )
        val queue = svc.resolveSmartQueue(setOf(1L), onlyWithMistakes = false)
        assertEquals(listOf(SmartGamePair(1L, 10L)), queue)
    }

    // region fakes

    private class FakeTrainingDao(private val trainings: List<TrainingEntity>) : TrainingDao {
        override suspend fun insert(training: TrainingEntity) = training.id
        override suspend fun getAll() = trainings
        override suspend fun getById(id: Long) = trainings.firstOrNull { it.id == id }
        override suspend fun getFirst() = trainings.firstOrNull()
        override suspend fun deleteById(id: Long) {}
        override suspend fun update(training: TrainingEntity) {}
    }

    private class FakeTrainingResultDao(
        private val results: List<TrainingResultEntity>,
    ) : TrainingResultDao() {
        override suspend fun insertInternal(result: TrainingResultEntity) = 0L
        override suspend fun trimToLatestInternal(limit: Int) {}
        override suspend fun getRecentResults(limit: Int) = results.take(limit)
        override suspend fun getResultsForGame(gameId: Long, limit: Int) =
            results.filter { it.gameId == gameId }
                .sortedByDescending { it.trainedAt }
                .take(limit)
    }

    // endregion
}
