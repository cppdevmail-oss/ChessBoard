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

import com.example.chessboard.boardmodel.LineDraft
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.service.SmartLinePair
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineResult
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
        assertEquals(emptyList<SmartLinePair>(), runtimeContext.smartTrainingQueue)
    }

    @Test
    fun `startTraining stores queue and navigates to first smart line`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        val queue = listOf(
            SmartLinePair(trainingId = 1L, lineId = 10L),
            SmartLinePair(trainingId = 2L, lineId = 20L),
        )

        val result = coordinator.startTraining(queue)

        assertEquals(queue, runtimeContext.smartTrainingQueue)
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTrainLine(1L, 10L)),
            result,
        )
    }

    @Test
    fun `hasNextLine and session counters use smart queue`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartLinePair(trainingId = 1L, lineId = 10L),
            SmartLinePair(trainingId = 2L, lineId = 20L),
            SmartLinePair(trainingId = 3L, lineId = 30L),
        )

        assertTrue(coordinator.hasNextLine(10L))
        assertEquals(2, coordinator.sessionCurrent(20L))
        assertEquals(3, coordinator.sessionTotal())
    }

    @Test
    fun `finishLine navigates back to smart training home`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)

        val result = coordinator.finishLine()

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTraining),
            result,
        )
    }

    @Test
    fun `openNextLine navigates to next smart line when queue has one`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartLinePair(trainingId = 1L, lineId = 10L),
            SmartLinePair(trainingId = 2L, lineId = 20L),
        )

        val result = coordinator.openNextLine(
            TrainSingleLineResult(
                lineId = 10L,
                trainingId = 1L,
                mistakesCount = 0,
            )
        )

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.SmartTrainLine(2L, 20L)),
            result,
        )
    }

    @Test
    fun `openNextLine navigates to smart training home when queue is exhausted`() {
        val runtimeContext = RuntimeContext()
        val coordinator = SmartTrainingFlowCoordinator(runtimeContext)
        runtimeContext.smartTrainingQueue = listOf(
            SmartLinePair(trainingId = 1L, lineId = 10L),
        )

        val result = coordinator.openNextLine(
            TrainSingleLineResult(
                lineId = 10L,
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
        val line = lineEntity(lineId = 30L)
        val draft = LineDraft(line = line)

        assertEquals(
            TrainingFlowResult.OpenLineEditor(
                line = line,
                backTarget = ScreenType.SmartTrainLine(5L, 30L),
            ),
            coordinator.openLineEditor(
                line = line,
                trainingId = 5L,
                lineId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenCreateOpening(
                draft = draft,
                backTarget = ScreenType.SmartTrainLine(5L, 30L),
            ),
            coordinator.openCreateOpening(
                draft = draft,
                trainingId = 5L,
                lineId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenPositionSearch(
                initialFen = "fen",
                backTarget = ScreenType.SmartTrainLine(5L, 30L),
            ),
            coordinator.openPositionSearch(
                fen = "fen",
                trainingId = 5L,
                lineId = 30L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenAnalysis(
                uciMoves = listOf("e2e4"),
                initialPly = 1,
                backTarget = ScreenType.SmartTrainLine(5L, 30L),
            ),
            coordinator.openAnalysis(
                trainingId = 5L,
                lineId = 30L,
                uciMoves = listOf("e2e4"),
                initialPly = 1,
            ),
        )
    }

    private fun lineEntity(lineId: Long): LineEntity {
        return LineEntity(
            id = lineId,
            pgn = "",
            initialFen = "",
        )
    }
}
