package com.example.chessboard.runtimecontext

/**
 * File role: groups unit tests for TrainingRuntimeContext session and progress behavior.
 * Allowed here:
 * - pure runtime-state tests for launch tracking, next-line resolution, and snapshot restore
 * - assertions about progress sanitization and training-session isolation
 * Not allowed here:
 * - Compose UI tests, navigation rendering checks, or persistence-backed behavior
 * - unrelated RuntimeContext wrapper tests that belong in separate test files
 * Validation date: 2026-04-26
 */

import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLinePhase
import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingRuntimeContextTest {

    @Test
    fun `rememberLaunch creates new session with active line and ordered ids`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 20L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(20L, runtimeContext.lineIdInTraining(1L))
        assertEquals(listOf(10L, 20L, 30L), runtimeContext.orderedLineIds(1L))
    }

    @Test
    fun `rememberLaunch replaces current line for existing session`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 30L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(30L, runtimeContext.lineIdInTraining(1L))
    }

    @Test
    fun `lineIdInTraining returns null for unknown training`() {
        val runtimeContext = TrainingRuntimeContext()

        assertNull(runtimeContext.lineIdInTraining(99L))
    }

    @Test
    fun `selectedLineId returns selected editor line when training is not active`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.setSelectedLineId(trainingId = 1L, lineId = 20L)

        assertEquals(20L, runtimeContext.selectedLineId(1L))
    }

    @Test
    fun `selectedLineId prefers lineIdInTraining over selected editor line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.setSelectedLineId(trainingId = 1L, lineId = 20L)

        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = 30L)

        assertEquals(30L, runtimeContext.selectedLineId(1L))
    }

    @Test
    fun `setSelectedLineId does not change lineIdInTraining`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = 30L)

        runtimeContext.setSelectedLineId(trainingId = 1L, lineId = 20L)

        assertEquals(30L, runtimeContext.lineIdInTraining(1L))
        assertEquals(30L, runtimeContext.selectedLineId(1L))
    }

    @Test
    fun `setLineIdInTraining null restores selected editor line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.setSelectedLineId(trainingId = 1L, lineId = 20L)
        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = 30L)

        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = null)

        assertNull(runtimeContext.lineIdInTraining(1L))
        assertEquals(20L, runtimeContext.selectedLineId(1L))
    }

    @Test
    fun `clearTrainingSession clears selected editor line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.setSelectedLineId(trainingId = 1L, lineId = 20L)
        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = 30L)

        runtimeContext.clearTrainingSession(1L)

        assertNull(runtimeContext.lineIdInTraining(1L))
        assertNull(runtimeContext.selectedLineId(1L))
    }

    @Test
    fun `firstStartedLineId returns null for unknown training`() {
        val runtimeContext = TrainingRuntimeContext()

        assertNull(runtimeContext.firstStartedLineId(99L))
    }

    @Test
    fun `firstStartedLineId returns null when session has no saved progress`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 20L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertNull(runtimeContext.firstStartedLineId(1L))
    }

    @Test
    fun `resolveNextLineId returns next item in ordered list`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        val nextLineId = runtimeContext.resolveNextLineId(
            trainingId = 1L,
            currentLineId = 20L,
        )

        assertEquals(30L, nextLineId)
    }

    @Test
    fun `resolveNextLineId returns null for last item`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        val nextLineId = runtimeContext.resolveNextLineId(
            trainingId = 1L,
            currentLineId = 30L,
        )

        assertNull(nextLineId)
    }

    @Test
    fun `resolveNextLineId returns null for missing current line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        val nextLineId = runtimeContext.resolveNextLineId(
            trainingId = 1L,
            currentLineId = 99L,
        )

        assertNull(nextLineId)
    }

    @Test
    fun `sessionCurrent returns one based index`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(1, runtimeContext.sessionCurrent(1L, 10L))
        assertEquals(2, runtimeContext.sessionCurrent(1L, 20L))
        assertEquals(3, runtimeContext.sessionCurrent(1L, 30L))
    }

    @Test
    fun `sessionCurrent falls back to one for unknown line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(1, runtimeContext.sessionCurrent(1L, 99L))
    }

    @Test
    fun `sessionTotal returns ordered list size`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )

        assertEquals(3, runtimeContext.sessionTotal(1L))
    }

    @Test
    fun `sessionTotal returns zero for unknown training`() {
        val runtimeContext = TrainingRuntimeContext()

        assertEquals(0, runtimeContext.sessionTotal(99L))
    }

    @Test
    fun `saveLineProgress stores snapshot for line`() {
        val runtimeContext = TrainingRuntimeContext()
        val uiState = trainUiState(
            phase = TrainSingleLinePhase.Training,
            expectedPly = 4,
            mistakesCount = 2,
        )

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 4,
            lineFingerprint = "line-a",
            uiState = uiState,
        )

        val restored = runtimeContext.restoreLineProgress(1L, 10L)

        assertEquals(4, restored?.currentPly)
        assertEquals("line-a", restored?.lineFingerprint)
        assertEquals(uiState, restored?.uiState)
    }

    @Test
    fun `firstStartedLineId returns first started line by stored order`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 30L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 30L,
            currentPly = 5,
            lineFingerprint = "line-c",
            uiState = trainUiState(expectedPly = 5),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 20L,
            currentPly = 3,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 3),
        )

        assertEquals(20L, runtimeContext.firstStartedLineId(1L))
    }

    @Test
    fun `saveLineProgress overwrites previous snapshot for same line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2, mistakesCount = 1),
        )

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 5,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 5, mistakesCount = 3),
        )

        val restored = runtimeContext.restoreLineProgress(1L, 10L)

        assertEquals(5, restored?.currentPly)
        assertEquals("line-b", restored?.lineFingerprint)
        assertEquals(5, restored?.uiState?.expectedPly)
        assertEquals(3, restored?.uiState?.mistakesCount)
    }

    @Test
    fun `saveLineProgress for one line does not affect another line`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 20L,
            currentPly = 6,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 6, mistakesCount = 4),
        )

        val first = runtimeContext.restoreLineProgress(1L, 10L)
        val second = runtimeContext.restoreLineProgress(1L, 20L)

        assertEquals(2, first?.currentPly)
        assertEquals(6, second?.currentPly)
        assertEquals(4, second?.uiState?.mistakesCount)
    }

    @Test
    fun `restoreLineProgress returns null for unsaved line`() {
        val runtimeContext = TrainingRuntimeContext()

        assertNull(runtimeContext.restoreLineProgress(1L, 10L))
    }

    @Test
    fun `clearLineProgress removes only target line snapshot`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 20L,
            currentPly = 4,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 4),
        )

        runtimeContext.clearLineProgress(trainingId = 1L, lineId = 10L)

        assertNull(runtimeContext.restoreLineProgress(1L, 10L))
        assertEquals(4, runtimeContext.restoreLineProgress(1L, 20L)?.currentPly)
    }

    @Test
    fun `clearLineProgress keeps session metadata`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 20L,
            orderedLineIds = listOf(10L, 20L, 30L),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 20L,
            currentPly = 4,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 4),
        )

        runtimeContext.clearLineProgress(trainingId = 1L, lineId = 20L)

        assertEquals(20L, runtimeContext.lineIdInTraining(1L))
        assertEquals(listOf(10L, 20L, 30L), runtimeContext.orderedLineIds(1L))
    }

    @Test
    fun `saveLineProgress clears wrongMoveSquare before storing`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 3,
            lineFingerprint = "line-a",
            uiState = trainUiState(
                phase = TrainSingleLinePhase.Mistake,
                wrongMoveSquare = "e4",
            ),
        )

        val restored = runtimeContext.restoreLineProgress(1L, 10L)

        assertNull(restored?.uiState?.wrongMoveSquare)
    }

    @Test
    fun `saveLineProgress converts ShowingLine phase to Idle`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 3,
            lineFingerprint = "line-a",
            uiState = trainUiState(phase = TrainSingleLinePhase.ShowingLine),
        )

        val restored = runtimeContext.restoreLineProgress(1L, 10L)

        assertEquals(TrainSingleLinePhase.Idle, restored?.uiState?.phase)
    }

    @Test
    fun `saveLineProgress preserves non ShowingLine phases`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 3,
            lineFingerprint = "line-a",
            uiState = trainUiState(phase = TrainSingleLinePhase.Training),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 20L,
            currentPly = 6,
            lineFingerprint = "line-b",
            uiState = trainUiState(phase = TrainSingleLinePhase.Mistake),
        )

        assertEquals(
            TrainSingleLinePhase.Training,
            runtimeContext.restoreLineProgress(1L, 10L)?.uiState?.phase,
        )
        assertEquals(
            TrainSingleLinePhase.Mistake,
            runtimeContext.restoreLineProgress(1L, 20L)?.uiState?.phase,
        )
    }

    @Test
    fun `sessions are isolated by trainingId`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2),
        )
        runtimeContext.saveLineProgress(
            trainingId = 2L,
            lineId = 10L,
            currentPly = 7,
            lineFingerprint = "line-b",
            uiState = trainUiState(expectedPly = 7),
        )

        assertEquals(2, runtimeContext.restoreLineProgress(1L, 10L)?.currentPly)
        assertEquals(7, runtimeContext.restoreLineProgress(2L, 10L)?.currentPly)
    }

    @Test
    fun `saveLineProgress updates active line`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 30L,
            currentPly = 3,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 3),
        )

        assertEquals(30L, runtimeContext.lineIdInTraining(1L))
    }

    @Test
    fun `setLineIdInTraining updates active line without touching snapshots`() {
        val runtimeContext = TrainingRuntimeContext()
        runtimeContext.rememberLaunch(
            trainingId = 1L,
            lineId = 10L,
            orderedLineIds = listOf(10L, 20L),
        )
        runtimeContext.saveLineProgress(
            trainingId = 1L,
            lineId = 10L,
            currentPly = 2,
            lineFingerprint = "line-a",
            uiState = trainUiState(expectedPly = 2),
        )

        runtimeContext.setLineIdInTraining(trainingId = 1L, lineId = 20L)

        assertEquals(20L, runtimeContext.lineIdInTraining(1L))
        assertEquals(2, runtimeContext.restoreLineProgress(1L, 10L)?.currentPly)
    }

    @Test
    fun `setLineIdInTraining creates session for unknown training`() {
        val runtimeContext = TrainingRuntimeContext()

        runtimeContext.setLineIdInTraining(trainingId = 99L, lineId = 20L)

        assertEquals(20L, runtimeContext.lineIdInTraining(99L))
    }

    private fun trainUiState(
        phase: TrainSingleLinePhase = TrainSingleLinePhase.Idle,
        expectedPly: Int = 0,
        mistakesCount: Int = 0,
        wrongMoveSquare: String? = null,
    ): TrainSingleLineUiState {
        return TrainSingleLineUiState(
            phase = phase,
            expectedPly = expectedPly,
            mistakesCount = mistakesCount,
            wrongMoveSquare = wrongMoveSquare,
        )
    }
}
