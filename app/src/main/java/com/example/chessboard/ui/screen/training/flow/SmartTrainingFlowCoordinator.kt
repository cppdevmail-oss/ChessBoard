package com.example.chessboard.ui.screen.training.flow

/**
 * File role: groups smart-training flow orchestration and transition rules.
 * Allowed here:
 * - runtime-context updates and navigation decisions for smart-training queue traversal
 * - smart-training side-destination routing such as analysis, line editor, and create-opening entry points
 * Not allowed here:
 * - composable UI, activity-owned state, or regular-training ordering logic
 * - database access, repository calls, or persistence logic
 * Validation date: 2026-04-26
 */

import com.example.chessboard.boardmodel.LineDraft
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.service.SmartLinePair
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineResult

class SmartTrainingFlowCoordinator(
    private val runtimeContext: RuntimeContext,
) {
    fun startTraining(queue: List<SmartLinePair>): TrainingFlowResult? {
        val first = queue.firstOrNull() ?: return null
        runtimeContext.smartTrainingQueue = queue
        return TrainingFlowResult.Navigate(
            ScreenType.SmartTrainLine(first.trainingId, first.lineId)
        )
    }

    fun hasNextLine(lineId: Long): Boolean {
        return runtimeContext.resolveNextSmartLinePair(lineId) != null
    }

    fun sessionCurrent(lineId: Long): Int {
        return runtimeContext.smartTrainingQueue
            .indexOfFirst { it.lineId == lineId }
            .coerceAtLeast(0) + 1
    }

    fun sessionTotal(): Int {
        return runtimeContext.smartTrainingQueue.size
    }

    fun finishLine(): TrainingFlowResult {
        return TrainingFlowResult.Navigate(ScreenType.SmartTraining)
    }

    fun interruptTraining(trainingId: Long): TrainingFlowResult {
        runtimeContext.trainingSession.clearTrainingSession(trainingId)
        runtimeContext.smartTrainingQueue = emptyList()
        return TrainingFlowResult.Navigate(ScreenType.SmartTraining)
    }

    fun openNextLine(result: TrainSingleLineResult): TrainingFlowResult {
        val nextLine = runtimeContext.resolveNextSmartLinePair(result.lineId)
        if (nextLine == null) {
            return TrainingFlowResult.Navigate(ScreenType.SmartTraining)
        }

        return TrainingFlowResult.Navigate(
            ScreenType.SmartTrainLine(nextLine.trainingId, nextLine.lineId)
        )
    }

    fun openLineEditor(
        line: LineEntity,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenLineEditor(
            line = line,
            backTarget = ScreenType.SmartTrainLine(trainingId, lineId),
        )
    }

    fun openCreateOpening(
        draft: LineDraft,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenCreateOpening(
            draft = draft,
            backTarget = ScreenType.SmartTrainLine(trainingId, lineId),
        )
    }

    fun openPositionSearch(
        fen: String,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenPositionSearch(
            initialFen = fen,
            backTarget = ScreenType.SmartTrainLine(trainingId, lineId),
        )
    }

    fun openAnalysis(
        trainingId: Long,
        lineId: Long,
        uciMoves: List<String>,
        initialPly: Int,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenAnalysis(
            uciMoves = uciMoves,
            initialPly = initialPly,
            backTarget = ScreenType.SmartTrainLine(trainingId, lineId),
        )
    }
}
