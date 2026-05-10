package com.example.chessboard.ui.screen.trainSingleLine

/**
 * File role: groups the launch/loading layer for a single-line training screen.
 * Allowed here:
 * - training-line data loading, launch-time validation, and error routing
 * - wiring from loaded launch data into the actual training screen container
 * Not allowed here:
 * - long-lived session state that belongs in runtimecontext
 * - detailed training-screen interaction logic that belongs in TrainSingleLineScreen
 * Validation date: 2026-04-25
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.boardmodel.LineDraft
import com.example.chessboard.entity.LineEntity
import com.example.chessboard.runtimecontext.TrainingRuntimeContext
import com.example.chessboard.service.TrainingLineLaunchBrokenTrainingDeleted
import com.example.chessboard.service.TrainingLineLaunchLineNotFound
import com.example.chessboard.service.TrainingLineLaunchLineRemovedFromTraining
import com.example.chessboard.service.TrainingLineLaunchNoTrainings
import com.example.chessboard.service.TrainingLineLaunchReady
import com.example.chessboard.service.TrainingLineLaunchTrainingNotFound
import com.example.chessboard.service.parsePgnMoves
import com.example.chessboard.service.uciMovesToMoves
import com.example.chessboard.ui.components.AppMessageDialog
import com.github.bhlangonijr.chesslib.Board
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.screen.ScreenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface TrainSingleLineLaunchState {
    data object Loading : TrainSingleLineLaunchState
    data class Ready(
        val trainingLineData: TrainSingleLineData,
        val autoNextLine: Boolean,
    ) : TrainSingleLineLaunchState
    data object TrainingNotFound : TrainSingleLineLaunchState
    data object LineNotFound : TrainSingleLineLaunchState
    data object LineRemovedFromTraining : TrainSingleLineLaunchState
}

@Composable
// Loads the training line data or shows a launch error before opening the training screen.
fun TrainSingleLineLauncherScreenContainer(
    trainingId: Long,
    lineId: Long,
    moveFrom: Int = 1,
    moveTo: Int = 0,
    trainingRuntimeContext: TrainingRuntimeContext,
    keepLineIfZero: Boolean = false,
    hasNextTrainingLine: Boolean = false,
    sessionCurrent: Int = 0,
    sessionTotal: Int = 0,
    onTrainingFinished: (TrainSingleLineResult) -> Unit = {},
    onNextTrainingClick: (TrainSingleLineResult) -> Unit = {},
    onInterruptTrainingClick: () -> Unit,
    onOpenLineEditorClick: (LineEntity) -> Unit,
    onCloneLineClick: (LineDraft) -> Unit,
    onSearchByPositionClick: (String) -> Unit,
    onAnalyzeLineClick: (List<String>, Int) -> Unit,
    simpleViewEnabled: Boolean = false,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
) {
    val inDbProvider = screenContext.inDbProvider
    val trainSingleLineService = remember(inDbProvider) {
        inDbProvider.createTrainSingleLineService()
    }
    var launchState by remember { mutableStateOf<TrainSingleLineLaunchState>(TrainSingleLineLaunchState.Loading) }

    fun clearInvalidTrainingRuntimeState() {
        trainingRuntimeContext.clearLineProgress(trainingId, lineId)
        if (trainingRuntimeContext.activeLineId(trainingId) == lineId) {
            trainingRuntimeContext.setCurrentLineId(trainingId, null)
        }
    }

    LaunchedEffect(trainingId, lineId) {
        launchState = withContext(Dispatchers.IO) {
            when (val launchData = trainSingleLineService.getTrainingLineLaunchData(trainingId, lineId)) {
                is TrainingLineLaunchTrainingNotFound,
                is TrainingLineLaunchBrokenTrainingDeleted,
                TrainingLineLaunchNoTrainings -> {
                    return@withContext TrainSingleLineLaunchState.TrainingNotFound
                }

                is TrainingLineLaunchLineNotFound -> {
                    return@withContext TrainSingleLineLaunchState.LineNotFound
                }

                is TrainingLineLaunchLineRemovedFromTraining -> {
                    return@withContext TrainSingleLineLaunchState.LineRemovedFromTraining
                }

                is TrainingLineLaunchReady -> {
                    val line = trainSingleLineService.loadLine(launchData.launchData.lineId)
                        ?: return@withContext TrainSingleLineLaunchState.LineNotFound

                    val allMoves = parsePgnMoves(line.pgn)
                    val startPly = ((moveFrom - 1) * 2).coerceAtMost(allMoves.size)
                    val endPly = if (moveTo > 0) (moveTo * 2).coerceAtMost(allMoves.size) else allMoves.size
                    val moves = allMoves.subList(startPly, endPly)
                    val startFen = if (startPly == 0) null else {
                        val board = Board()
                        uciMovesToMoves(allMoves.take(startPly)).forEach { board.doMove(it) }
                        board.fen
                    }
                    val autoNextLine = screenContext.inDbProvider
                        .createUserProfileService()
                        .getProfile()
                        .autoNextLine

                    return@withContext TrainSingleLineLaunchState.Ready(
                        trainingLineData = TrainSingleLineData(
                            line = line,
                            uciMoves = moves,
                            startFen = startFen,
                            hasMoveCap = moveTo > 0,
                            analysisUciMoves = allMoves,
                            analysisStartPly = startPly,
                        ),
                        autoNextLine = autoNextLine,
                    )
                }
            }
        }
    }

    if (launchState is TrainSingleLineLaunchState.TrainingNotFound) {
        LaunchedEffect(launchState) {
            clearInvalidTrainingRuntimeState()
        }
        TrainingLaunchErrorDialog(
            title = "Training not found",
            message = "The selected training is unavailable to start.",
            onDismiss = { screenContext.onNavigate(ScreenType.Training) },
        )
        return
    }

    if (launchState is TrainSingleLineLaunchState.LineNotFound) {
        LaunchedEffect(launchState) {
            clearInvalidTrainingRuntimeState()
        }
        TrainingLaunchErrorDialog(
            title = "Line not found",
            message = "The selected line is unavailable to start.",
            onDismiss = { screenContext.onNavigate(ScreenType.EditTraining(trainingId)) },
        )
        return
    }

    if (launchState is TrainSingleLineLaunchState.LineRemovedFromTraining) {
        LaunchedEffect(launchState) {
            clearInvalidTrainingRuntimeState()
        }
        TrainingLaunchErrorDialog(
            title = "Line removed from training",
            message = "The selected line no longer belongs to this training.",
            onDismiss = { screenContext.onNavigate(ScreenType.EditTraining(trainingId)) },
        )
        return
    }

    val readyState = launchState as? TrainSingleLineLaunchState.Ready ?: return

    TrainSingleLineScreenContainer(
        lineId = lineId,
        trainingId = trainingId,
        trainingLineData = readyState.trainingLineData,
        trainingRuntimeContext = trainingRuntimeContext,
        keepLineIfZero = keepLineIfZero,
        hasNextTrainingLine = hasNextTrainingLine,
        sessionCurrent = sessionCurrent,
        sessionTotal = sessionTotal,
        onTrainingFinished = onTrainingFinished,
        onNextTrainingClick = onNextTrainingClick,
        onInterruptTrainingClick = onInterruptTrainingClick,
        onOpenLineEditorClick = { onOpenLineEditorClick(readyState.trainingLineData.line) },
        onCloneLineClick = { onCloneLineClick(it) },
        onSearchByPositionClick = onSearchByPositionClick,
        onAnalyzeLineClick = onAnalyzeLineClick,
        autoNextLine = readyState.autoNextLine,
        simpleViewEnabled = simpleViewEnabled,
        screenContext = screenContext,
        modifier = modifier,
    )
}

@Composable
private fun TrainingLaunchErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    AppMessageDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
    )
}
