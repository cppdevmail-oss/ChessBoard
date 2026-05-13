package com.example.chessboard.ui.screen.training.flow

/**
 * File role: groups unit tests for regular training-flow coordination.
 * Allowed here:
 * - runtime-only tests for regular training navigation decisions and runtime-context updates
 * - assertions about back targets and next-line transitions returned by the regular coordinator
 * Not allowed here:
 * - Compose UI tests, activity wiring checks, or smart-training queue behavior
 * - database-backed integration tests or persistence behavior
 * Validation date: 2026-04-26
 */

import com.example.chessboard.boardmodel.LineDraft
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegularTrainingFlowCoordinatorTest {

    @Test
    fun `openTraining resets ordering and navigates to editor`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        val lines = listOf(
            weightedLine(lineId = 10L, weight = 5),
            weightedLine(lineId = 20L, weight = 3),
        )
        runtimeContext.orderLinesInTraining.markLineCompleted(10L)

        coordinator.openTraining(trainingId = 5L)

        val orderedIds = runtimeContext.orderLinesInTraining
            .orderLines(
                lines = lines,
                getLineId = { it.lineId },
                getWeight = { it.weight },
            )
            .map { it.lineId }

        val result = coordinator.openTraining(trainingId = 5L)

        assertEquals(listOf(10L, 20L), orderedIds)
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.EditTraining(5L)),
            result,
        )
    }

    @Test
    fun `openTraining navigates directly to first started line by stored order`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 5L,
            lineId = 30L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )
        runtimeContext.trainingSession.saveLineProgress(
            trainingId = 5L,
            lineId = 30L,
            currentPly = 4,
            lineFingerprint = "line-c",
            uiState = trainUiState(expectedPly = 4),
        )
        runtimeContext.trainingSession.saveLineProgress(
            trainingId = 5L,
            lineId = 20L,
            currentPly = 2,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 2),
        )

        val result = coordinator.openTraining(trainingId = 5L)

        assertEquals(20L, runtimeContext.trainingSession.lineIdInTraining(5L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.TrainSingleLine(5L, 20L)),
            result,
        )
    }

    @Test
    fun `startLine remembers launch and navigates to single line`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)

        val result = coordinator.startLine(
            trainingId = 7L,
            lineId = 20L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(20L, runtimeContext.trainingSession.lineIdInTraining(7L))
        assertEquals(listOf(10L, 20L, 30L), runtimeContext.trainingSession.orderedLineIds(7L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.TrainSingleLine(7L, 20L)),
            result,
        )
    }

    @Test
    fun `finishLine clears progress resets active line and navigates to editor`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L),
        )
        runtimeContext.trainingSession.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 4,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 4),
        )

        val result = coordinator.finishLine(
            TrainSingleLineResult(
                lineId = 10L,
                trainingId = 1L,
                mistakesCount = 2,
            )
        )

        assertNull(runtimeContext.trainingSession.restoreLineProgress(1L, 10L))
        assertNull(runtimeContext.trainingSession.lineIdInTraining(1L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.EditTraining(1L)),
            result,
        )
    }

    @Test
    fun `interruptTraining clears whole training session and navigates to editor`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L),
        )
        runtimeContext.trainingSession.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 4,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 4),
        )
        runtimeContext.orderLinesInTraining.markLineCompleted(10L)

        val result = coordinator.interruptTraining(trainingId = 1L)

        assertNull(runtimeContext.trainingSession.lineIdInTraining(1L))
        assertEquals(emptyList<Long>(), runtimeContext.trainingSession.orderedLineIds(1L))
        assertNull(runtimeContext.trainingSession.restoreLineProgress(1L, 10L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.EditTraining(1L)),
            result,
        )
    }

    @Test
    fun `openNextLine moves to next stored line`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )
        runtimeContext.trainingSession.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2),
        )

        val result = coordinator.openNextLine(
            TrainSingleLineResult(
                lineId = 10L,
                trainingId = 1L,
                mistakesCount = 0,
            )
        )

        assertNull(runtimeContext.trainingSession.restoreLineProgress(1L, 10L))
        assertEquals(20L, runtimeContext.trainingSession.lineIdInTraining(1L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.TrainSingleLine(1L, 20L)),
            result,
        )
    }

    @Test
    fun `openNextLine falls back to editor when next line is missing`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            lineId = 30L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        val result = coordinator.openNextLine(
            TrainSingleLineResult(
                lineId = 30L,
                trainingId = 1L,
                mistakesCount = 1,
            )
        )

        assertNull(runtimeContext.trainingSession.lineIdInTraining(1L))
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.EditTraining(1L)),
            result,
        )
    }

    @Test
    fun `openSettings and closeSettings navigate around editor`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)

        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.TrainingSettings(9L)),
            coordinator.openSettings(9L),
        )
        assertEquals(
            TrainingFlowResult.Navigate(ScreenType.EditTraining(9L)),
            coordinator.closeSettings(9L),
        )
    }

    @Test
    fun `hasNextLine and session counters delegate to training session order`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 3L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertTrue(coordinator.hasNextLine(trainingId = 3L, lineId = 10L))
        assertEquals(2, coordinator.sessionCurrent(trainingId = 3L, lineId = 20L))
        assertEquals(3, coordinator.sessionTotal(trainingId = 3L))
    }

    @Test
    fun `side destination results from editor use edit training back target`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        val line = lineEntity(lineId = 50L)

        assertEquals(
            TrainingFlowResult.OpenLineEditor(
                line = line,
                backTarget = ScreenType.EditTraining(4L),
            ),
            coordinator.openLineEditorFromEditor(line = line, trainingId = 4L),
        )
    }

    @Test
    fun `side destination results from training use single line back target`() {
        val runtimeContext = RuntimeContext()
        val coordinator = RegularTrainingFlowCoordinator(runtimeContext)
        val line = lineEntity(lineId = 60L)
        val draft = LineDraft(line = line)

        assertEquals(
            TrainingFlowResult.OpenLineEditor(
                line = line,
                backTarget = ScreenType.TrainSingleLine(8L, 60L),
            ),
            coordinator.openLineEditorFromTraining(
                line = line,
                trainingId = 8L,
                lineId = 60L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenCreateOpening(
                draft = draft,
                backTarget = ScreenType.TrainSingleLine(8L, 60L),
            ),
            coordinator.openCreateOpeningFromTraining(
                draft = draft,
                trainingId = 8L,
                lineId = 60L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenPositionSearch(
                initialFen = "fen",
                backTarget = ScreenType.TrainSingleLine(8L, 60L),
            ),
            coordinator.openPositionSearchFromTraining(
                fen = "fen",
                trainingId = 8L,
                lineId = 60L,
            ),
        )
        assertEquals(
            TrainingFlowResult.OpenAnalysis(
                uciMoves = listOf("e2e4", "e7e5"),
                initialPly = 2,
                backTarget = ScreenType.TrainSingleLine(8L, 60L),
            ),
            coordinator.openAnalysisFromTraining(
                trainingId = 8L,
                lineId = 60L,
                uciMoves = listOf("e2e4", "e7e5"),
                initialPly = 2,
            ),
        )
    }

    private data class WeightedLine(
        val lineId: Long,
        val weight: Int,
    )

    private fun weightedLine(
        lineId: Long,
        weight: Int,
    ): WeightedLine {
        return WeightedLine(
            lineId = lineId,
            weight = weight,
        )
    }

    private fun lineEntity(lineId: Long): LineEntity {
        return LineEntity(
            id = lineId,
            pgn = "",
            initialFen = "",
        )
    }

    private fun trainUiState(expectedPly: Int) =
        com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineUiState(
            expectedPly = expectedPly,
        )
}
