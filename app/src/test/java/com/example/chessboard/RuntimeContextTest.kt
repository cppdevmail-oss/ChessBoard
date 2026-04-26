package com.example.chessboard

/**
 * File role: groups unit tests for RuntimeContext wrappers around training-session helpers.
 * Allowed here:
 * - small runtime-only tests for RuntimeContext delegation to training session state
 * - assertions about next-training resolution through the public RuntimeContext API
 * Not allowed here:
 * - UI tests, Compose rendering checks, or database-backed behavior
 * - deep unit coverage for TrainingRuntimeContext internals that belongs in its own test file
 * Validation date: 2026-04-26
 */

import com.example.chessboard.runtimecontext.RuntimeContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RuntimeContextTest {

    @Test
    fun `resolveNextTrainingGameId returns next id from stored order`() {
        val runtimeContext = RuntimeContext()
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            gameId = 10L,
            orderedGameIds = listOf(10L, 20L, 30L),
        )

        val nextGameId = runtimeContext.resolveNextTrainingGameId(
            trainingId = 1L,
            currentGameId = 10L,
        )

        assertEquals(20L, nextGameId)
    }

    @Test
    fun `resolveNextTrainingGameId returns null for last game in stored order`() {
        val runtimeContext = RuntimeContext()
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            gameId = 10L,
            orderedGameIds = listOf(10L, 20L, 30L),
        )

        val nextGameId = runtimeContext.resolveNextTrainingGameId(
            trainingId = 1L,
            currentGameId = 30L,
        )

        assertNull(nextGameId)
    }

    @Test
    fun `resolveNextTrainingGameId returns null when current game is missing`() {
        val runtimeContext = RuntimeContext()
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            gameId = 10L,
            orderedGameIds = listOf(10L, 20L, 30L),
        )

        val nextGameId = runtimeContext.resolveNextTrainingGameId(
            trainingId = 1L,
            currentGameId = 99L,
        )

        assertNull(nextGameId)
    }

    @Test
    fun `resolveNextTrainingGameId keeps using stored order after completion marks`() {
        val runtimeContext = RuntimeContext()
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = 1L,
            gameId = 10L,
            orderedGameIds = listOf(10L, 20L, 30L),
        )
        runtimeContext.orderGamesInTraining.markGameCompleted(10L)

        val nextGameId = runtimeContext.resolveNextTrainingGameId(
            trainingId = 1L,
            currentGameId = 10L,
        )

        assertEquals(20L, nextGameId)
    }
}
