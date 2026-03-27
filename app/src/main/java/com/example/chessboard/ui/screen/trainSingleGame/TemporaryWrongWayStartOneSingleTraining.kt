package com.example.chessboard.ui.screen.trainSingleGame

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.FirstTrainingGameLaunchBrokenTrainingDeleted
import com.example.chessboard.service.FirstTrainingGameLaunchNoTrainings
import com.example.chessboard.service.FirstTrainingGameLaunchReady
import com.example.chessboard.service.FirstTrainingGameLaunchResult
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.SectionTitleText
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.theme.TrainingTextPrimary
import com.example.chessboard.ui.theme.TrainingTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Temporarily resolves the first available single-game training before opening the clean screen.
@Composable
fun TemporaryWrongWayStartOneSingleTraining(
    onTrainingFinished: (TrainSingleGameResult) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier,
    inDbProvider: DatabaseProvider,
) {
    var launchResult by remember { mutableStateOf<FirstTrainingGameLaunchResult?>(null) }

    LaunchedEffect(Unit) {
        launchResult = withContext(Dispatchers.IO) {
            inDbProvider.getFirstTrainingGameLaunchData()
        }
    }

    val resolvedResult = launchResult ?: return

    if (resolvedResult is FirstTrainingGameLaunchReady) {
        TrainSingleGameScreenContainer(
            gameId = resolvedResult.launchData.gameId,
            trainingId = resolvedResult.launchData.trainingId,
            onTrainingFinished = onTrainingFinished,
            onBackClick = onBackClick,
            onNavigate = onNavigate,
            modifier = modifier,
            inDbProvider = inDbProvider
        )
        return
    }

    if (resolvedResult is FirstTrainingGameLaunchNoTrainings) {
        TemporaryWrongWayStartTrainingErrorDialog(
            title = "No trainings found",
            message = "There are no trainings available to start.",
            onDismiss = onBackClick
        )
        return
    }

    if (resolvedResult is FirstTrainingGameLaunchBrokenTrainingDeleted) {
        TemporaryWrongWayStartTrainingErrorDialog(
            title = "Broken training deleted",
            message = "The first training was broken and has been deleted.",
            onDismiss = onBackClick
        )
    }
}

@Composable
private fun TemporaryWrongWayStartTrainingErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            SectionTitleText(text = title, color = TrainingTextPrimary)
        },
        text = {
            BodySecondaryText(text = message, color = TrainingTextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                BodySecondaryText(text = "OK", color = TrainingTextPrimary)
            }
        }
    )
}
