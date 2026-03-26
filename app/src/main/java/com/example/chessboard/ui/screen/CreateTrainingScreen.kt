package com.example.chessboard.ui.screen

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.ui.components.AppBottomNavigation
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.AppTopBar
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.ScreenSection
import com.example.chessboard.ui.components.defaultAppBottomNavigationItems
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TrainingBackgroundDark
import com.example.chessboard.ui.theme.TrainingTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEFAULT_TRAINING_NAME = "FullTraining"

data class TrainingGameEditorItem(
    val gameId: Long,
    val title: String,
    val weight: Int = 1
)

@Composable
fun CreateTrainingScreenContainer(
    activity: Activity,
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier,
    inDbProvider: DatabaseProvider,
) {
    var gamesForTraining by remember { mutableStateOf<List<TrainingGameEditorItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        gamesForTraining = withContext(Dispatchers.IO) {
            inDbProvider.getAllGames().map { game ->
                game.toTrainingGameEditorItem()
            }
        }
    }

    CreateTrainingScreen(
        gamesForTraining = gamesForTraining,
        onBackClick = onBackClick,
        onNavigate = onNavigate,
        modifier = modifier
    )
}

@Composable
fun CreateTrainingScreen(
    gamesForTraining: List<TrainingGameEditorItem> = emptyList(),
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavItem by remember { mutableStateOf<ScreenType>(ScreenType.Home) }
    var trainingName by remember { mutableStateOf(DEFAULT_TRAINING_NAME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TrainingBackgroundDark,
        topBar = {
            AppTopBar(
                title = "Create Training",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            CreateTrainingBottomNavigation(
                selectedItem = selectedNavItem,
                onItemSelected = {
                    selectedNavItem = it
                    onNavigate(it)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(AppDimens.spaceLg))
            ScreenSection {
                AppTextField(
                    value = trainingName,
                    onValueChange = { trainingName = it },
                    label = "Training Name",
                    placeholder = DEFAULT_TRAINING_NAME
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.spaceLg))

            ScreenSection {
                BodySecondaryText(
                    text = "Games loaded for training: ${gamesForTraining.size}",
                    color = TrainingTextSecondary
                )
            }
        }
    }
}

private fun GameEntity.toTrainingGameEditorItem(): TrainingGameEditorItem {
    return TrainingGameEditorItem(
        gameId = id,
        title = event ?: "Unnamed Opening",
        weight = 1
    )
}

@Composable
private fun CreateTrainingBottomNavigation(
    selectedItem: ScreenType,
    onItemSelected: (ScreenType) -> Unit,
    modifier: Modifier = Modifier
) {
    AppBottomNavigation(
        items = defaultAppBottomNavigationItems(),
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        modifier = modifier
    )
}
