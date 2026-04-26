package com.example.chessboard.ui.screen.training.flow

/**
 * File role: groups app-level navigation results emitted by training flow coordinators.
 * Allowed here:
 * - sealed result types that describe where the app should navigate after a training-flow action
 * - payload models needed to open app-level side destinations from training flows
 * Not allowed here:
 * - coordinator transition logic or runtime-context mutation code
 * - composable UI, screen rendering, or persistence helpers
 * Validation date: 2026-04-26
 */

import com.example.chessboard.boardmodel.GameDraft
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.ui.screen.ScreenType

sealed interface TrainingFlowResult {
    data class Navigate(val screen: ScreenType) : TrainingFlowResult

    data class OpenGameEditor(
        val game: GameEntity,
        val backTarget: ScreenType,
    ) : TrainingFlowResult

    data class OpenCreateOpening(
        val draft: GameDraft,
        val backTarget: ScreenType,
    ) : TrainingFlowResult

    data class OpenPositionEditor(
        val initialFen: String,
        val backTarget: ScreenType,
    ) : TrainingFlowResult

    data class OpenAnalysis(
        val uciMoves: List<String>,
        val initialPly: Int,
        val backTarget: ScreenType,
    ) : TrainingFlowResult
}
