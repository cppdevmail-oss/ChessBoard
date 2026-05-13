package com.example.chessboard.ui.screen.training.flow

/**
 * File role: groups regular training-flow orchestration and transition rules.
 * Allowed here:
 * - runtime-context updates and navigation decisions for training editor, settings, and single-line training
 * - training-specific side-destination routing such as analysis, line editor, and create-opening entry points
 * Not allowed here:
 * - composable UI, activity state holders, or generic app navigation unrelated to training
 * - database access, repository calls, or persistence logic
 * Validation date: 2026-04-26
 */

import com.example.chessboard.boardmodel.LineDraft
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.runtimecontext.RuntimeContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.trainSingleLine.TrainSingleLineResult

class RegularTrainingFlowCoordinator(
    private val runtimeContext: RuntimeContext,
) {
    fun openTraining(trainingId: Long): TrainingFlowResult {
        val startedLineId = runtimeContext.trainingSession.firstStartedLineId(trainingId)
        if (startedLineId != null) {
            runtimeContext.trainingSession.setLineIdInTraining(trainingId, startedLineId)
            return TrainingFlowResult.Navigate(
                ScreenType.TrainSingleLine(trainingId, startedLineId)
            )
        }

        runtimeContext.orderLinesInTraining.reset()
        return TrainingFlowResult.Navigate(ScreenType.EditTraining(trainingId))
    }

    fun openSettings(trainingId: Long): TrainingFlowResult {
        return TrainingFlowResult.Navigate(ScreenType.TrainingSettings(trainingId))
    }

    fun closeSettings(trainingId: Long): TrainingFlowResult {
        return TrainingFlowResult.Navigate(ScreenType.EditTraining(trainingId))
    }

    fun startLine(
        trainingId: Long,
        lineId: Long,
        orderedLineIds: List<Long>,
    ): TrainingFlowResult {
        runtimeContext.trainingSession.rememberLaunch(
            trainingId = trainingId,
            lineId = lineId,
            orderedLineIds = orderedLineIds,
        )
        return TrainingFlowResult.Navigate(ScreenType.TrainSingleLine(trainingId, lineId))
    }

    fun hasNextLine(trainingId: Long, lineId: Long): Boolean {
        return runtimeContext.resolveNextTrainingLineId(trainingId, lineId) != null
    }

    fun sessionCurrent(trainingId: Long, lineId: Long): Int {
        return runtimeContext.trainingSession.sessionCurrent(trainingId, lineId)
    }

    fun sessionTotal(trainingId: Long): Int {
        return runtimeContext.trainingSession.sessionTotal(trainingId)
    }

    fun finishLine(result: TrainSingleLineResult): TrainingFlowResult {
        runtimeContext.trainingSession.clearLineProgress(
            trainingId = result.trainingId,
            lineId = result.lineId,
        )
        runtimeContext.trainingSession.setLineIdInTraining(
            trainingId = result.trainingId,
            lineId = null,
        )
        runtimeContext.orderLinesInTraining.markLineCompleted(result.lineId)
        return TrainingFlowResult.Navigate(ScreenType.EditTraining(result.trainingId))
    }

    fun interruptTraining(trainingId: Long): TrainingFlowResult {
        runtimeContext.trainingSession.clearTrainingSession(trainingId)
        runtimeContext.orderLinesInTraining.reset()
        return TrainingFlowResult.Navigate(ScreenType.EditTraining(trainingId))
    }

    fun openNextLine(result: TrainSingleLineResult): TrainingFlowResult {
        runtimeContext.trainingSession.clearLineProgress(
            trainingId = result.trainingId,
            lineId = result.lineId,
        )
        runtimeContext.orderLinesInTraining.markLineCompleted(result.lineId)
        val nextLineId = runtimeContext.resolveNextTrainingLineId(
            trainingId = result.trainingId,
            currentLineId = result.lineId,
        )

        if (nextLineId == null) {
            runtimeContext.trainingSession.setLineIdInTraining(
                trainingId = result.trainingId,
                lineId = null,
            )
            return TrainingFlowResult.Navigate(ScreenType.EditTraining(result.trainingId))
        }

        runtimeContext.trainingSession.setLineIdInTraining(
            trainingId = result.trainingId,
            lineId = nextLineId,
        )
        return TrainingFlowResult.Navigate(
            ScreenType.TrainSingleLine(result.trainingId, nextLineId)
        )
    }

    fun openLineEditorFromEditor(line: LineEntity, trainingId: Long): TrainingFlowResult {
        return TrainingFlowResult.OpenLineEditor(
            line = line,
            backTarget = ScreenType.EditTraining(trainingId),
        )
    }

    fun openLineEditorFromTraining(
        line: LineEntity,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenLineEditor(
            line = line,
            backTarget = ScreenType.TrainSingleLine(trainingId, lineId),
        )
    }

    fun openCreateOpeningFromTraining(
        draft: LineDraft,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenCreateOpening(
            draft = draft,
            backTarget = ScreenType.TrainSingleLine(trainingId, lineId),
        )
    }

    fun openPositionSearchFromTraining(
        fen: String,
        trainingId: Long,
        lineId: Long,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenPositionSearch(
            initialFen = fen,
            backTarget = ScreenType.TrainSingleLine(trainingId, lineId),
        )
    }

    fun openAnalysisFromTraining(
        trainingId: Long,
        lineId: Long,
        uciMoves: List<String>,
        initialPly: Int,
    ): TrainingFlowResult {
        return TrainingFlowResult.OpenAnalysis(
            uciMoves = uciMoves,
            initialPly = initialPly,
            backTarget = ScreenType.TrainSingleLine(trainingId, lineId),
        )
    }
}
