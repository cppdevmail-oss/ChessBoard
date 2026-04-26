package com.example.chessboard.ui.screen.training.flow

/**
 * File role: groups unit tests for smart-training flow coordination.
 * Allowed here:
 * - runtime-only tests for smart-training queue traversal and navigation results
 * - assertions about smart-training side-destination back targets
 * Not allowed here:
 * - Compose UI tests, activity wiring checks, or regular-training ordering behavior
 * - database-backed integration tests or persistence behavior
 * Validation date: 2026-04-26
 */

import com.example.chessboard.boardmodel.GameDraft
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.service.SmartGamePair
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.trainSingleGame.TrainSingleGameResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartTrainingFlowCoordinatorTest {

    @Test
    fun `startTraining returns null for empty queue`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)

        val result = coordinator.startTraining(emptyList())

        assertNull(result)
        assertEquals(emptyList<SmartGamePair>(), runtimeContext.smartTrainingQueue)
    }

    @Test
    fun `startTraining stores queue and navigates to first smart game`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        val queue = listOf(
            SmartGamePair(trainingId = 1L, gameId = 10L),
            SmartGamePair(trainingId = 2L, gameId = 20L),
        )

        val result = coordinator.startTraining(queue)

        assertEquals(queue, runtimeContext.smartTrainingQueue)
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTrainGame(1L, 10L)),
            result,
        )
    }

    @Test
    fun `hasNextGame and session counters use smart queue`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartGamePair(trainingId = 1L, gameId = 10L),
            SmartGamePair(trainingId = 2L, gameId = 20L),
            SmartGamePair(trainingId = 3L, gameId = 30L),
        )

        assertTrue(coordinator.hasNextGame(10L))
        assertEquals(2, coordinator.sessionCurrent(20L))
        assertEquals(3, coordinator.sessionTotal())
    }

    @Test
    fun `finishGame navigates back to smart training home`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)

        val result = coordinator.finishGame()

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTraining),
            result,
        )
    }

    @Test
    fun `openNextGame navigates to next smart game when queue has one`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartGamePair(trainingId = 1L, gameId = 10L),
            SmartGamePair(trainingId = 2L, gameId = 20L),
        )

        val result = coordinator.openNextGame(
            TrainSingleGameResult(
                gameId = 10L,
                trainingId = 1L,
                mistakesCount = 0,
            )
        )

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTrainGame(2L, 20L)),
            result,
        )
    }

    @Test
    fun `openNextGame navigates to smart training home when queue is exhausted`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartGamePair(trainingId = 1L, gameId = 10L),
        )

        val result = coordinator.openNextGame(
            TrainSingleGameResult(
                gameId = 10L,
                trainingId = 1L,
                mistakesCount = 0,
            )
        )

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTraining),
            result,
        )
    }

    @Test
    fun `side destination results use smart training back target`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        val game = gameEntity(gameId = 30L)
        val draft = GameDraft(game = game)

        assertEquals(
            TrainingFlowResult.OpenGameEditor(
                game = game,
                backTarget = ScreenType.SmartTrainGame(5L, 30L),
            ),
            coordinator.openGameEditor(
                game = game,
                trainingId = 5L,
                gameId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenCreateOpening(
                draft = draft,
                backTarget = ScreenType.SmartTrainGame(5L, 30L),
            ),
            coordinator.openCreateOpening(
                draft = draft,
                trainingId = 5L,
                gameId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenPositionEditor(
                initialFen = "fen",
                backTarget = ScreenType.SmartTrainGame(5L, 30L),
            ),
            coordinator.openPositionEditor(
                fen = "fen",
                trainingId = 5L,
                gameId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenAnalysis(
                uciMoves = listOf("e2e4"),
                initialPly = 1,
                backTarget = ScreenType.SmartTrainGame(5L, 30L),
            ),
            coordinator.openAnalysis(
                trainingId = 5L,
                gameId = 30L,
                uciMoves = listOf("e2e4"),
                initialPly = 1,
            ),
        )
    }

    private fun gameEntity(gameId: Long): GameEntity {
        return GameEntity(
            id = gameId,
            pgn = "",
            initialFen = "",
        )
    }
}
