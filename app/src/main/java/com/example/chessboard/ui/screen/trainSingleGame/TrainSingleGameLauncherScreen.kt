package com.example.chessboard.ui.screen.trainSingleGame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.TrainingGameLaunchBrokenTrainingDeleted
import com.example.chessboard.service.TrainingGameLaunchReady
import com.example.chessboard.service.TrainingGameLaunchResult
import com.example.chessboard.service.TrainingGameLaunchTrainingNotFound
import com.example.chessboard.ui.components.AppMessageDialog
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.parsePgnMoves
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun resolveLaunchResult(
    dbProvider: DatabaseProvider,
    trainingId: Long
): TrainingGameLaunchResult {
    return dbProvider.getTrainingGameLaunchData(trainingId)
}

private fun resolveBrokenTrainingDeletedMessage(): String {
    return "The selected training was broken and has been deleted."
}

@Composable
fun TrainSingleGameLauncherScreenContainer(
    trainingId: Long,
    onTrainingFinished: (TrainSingleGameResult) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier,
    inDbProvider: DatabaseProvider,
) {
    var launchResult by remember { mutableStateOf<TrainingGameLaunchResult?>(null) }
    var trainingGameData by remember { mutableStateOf<TrainSingleGameData?>(null) }

    LaunchedEffect(trainingId) {
        val resolvedResult = withContext(Dispatchers.IO) {
            resolveLaunchResult(
                dbProvider = inDbProvider,
                trainingId = trainingId,
            )
        }
        launchResult = resolvedResult

        if (resolvedResult !is TrainingGameLaunchReady) {
            return@LaunchedEffect
        }

        trainingGameData = withContext(Dispatchers.IO) {
            val game = inDbProvider.loadTrainingGame(resolvedResult.launchData.gameId)
                ?: return@withContext null

            TrainSingleGameData(
                game = game,
                uciMoves = parsePgnMoves(game.pgn),
            )
        }
    }

    val resolvedResult = launchResult ?: return

    if (resolvedResult is TrainingGameLaunchReady) {
        val loadedTrainingGameData = trainingGameData ?: return

        TrainSingleGameScreenContainer(
            gameId = resolvedResult.launchData.gameId,
            trainingId = resolvedResult.launchData.trainingId,
            trainingGameData = loadedTrainingGameData,
            onTrainingFinished = onTrainingFinished,
            onBackClick = onBackClick,
            onNavigate = onNavigate,
            modifier = modifier,
            inDbProvider = inDbProvider,
        )
        return
    }

    if (resolvedResult is TrainingGameLaunchTrainingNotFound) {
        TrainingLaunchErrorDialog(
            title = "Training not found",
            message = "The selected training is unavailable to start.",
            onDismiss = { onNavigate(ScreenType.Training) },
        )
        return
    }

    if (resolvedResult is TrainingGameLaunchBrokenTrainingDeleted) {
        TrainingLaunchErrorDialog(
            title = "Broken training deleted",
            message = resolveBrokenTrainingDeletedMessage(),
            onDismiss = { onNavigate(ScreenType.Training) },
        )
    }
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
