package com.example.chessboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.ui.screen.CreateOpeningScreenContainer
import com.example.chessboard.ui.screen.CreateTrainingScreenContainer
import com.example.chessboard.ui.screen.EditTrainingScreenContainer
import com.example.chessboard.ui.screen.GameEditorScreenContainer
import com.example.chessboard.ui.screen.GamesExplorerScreenContainer
import com.example.chessboard.ui.screen.HomeScreenContainer
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.ProfileScreenContainer
import com.example.chessboard.ui.screen.SettingsScreenContainer
import com.example.chessboard.ui.screen.TrainingListScreenContainer
import com.example.chessboard.ui.screen.trainSingleGame.TrainSingleGameLauncherScreenContainer
import com.example.chessboard.ui.theme.ChessBoardTheme

class MainActivity : ComponentActivity() {

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (!hasFocus) {
            return
        }

        hideSystemBars()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbProvider = DatabaseProvider.createInstance(this)

        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            ChessBoardTheme {
                var currentScreen by remember { mutableStateOf<ScreenType>(ScreenType.Home) }
                var selectedGame by remember { mutableStateOf<GameEntity?>(null) }
                var simpleViewEnabled by remember { mutableStateOf(false) }

                when (val screen = currentScreen) {
                    ScreenType.Training -> TrainingListScreenContainer(
                        activity = this@MainActivity,
                        inDbProvider = dbProvider,
                        onBackClick = { currentScreen = ScreenType.Home },
                        onNavigate = { currentScreen = it },
                        onOpenTraining = { trainingId ->
                            currentScreen = ScreenType.EditTraining(trainingId)
                        },
                    )

                    ScreenType.GamesExplorer -> GamesExplorerScreenContainer(
                        activity = this@MainActivity,
                        inDbProvider = dbProvider,
                        onBackClick = { currentScreen = ScreenType.Home },
                        onNavigate = { currentScreen = it },
                    )

                    ScreenType.CreateOpening -> CreateOpeningScreenContainer(
                        activity = this@MainActivity,
                        onBackClick = { currentScreen = ScreenType.Home },
                        inDbProvider = dbProvider,
                    )

                    ScreenType.CreateTraining -> CreateTrainingScreenContainer(
                        activity = this@MainActivity,
                        onBackClick = { currentScreen = ScreenType.Training },
                        onNavigate = { currentScreen = it },
                        inDbProvider = dbProvider,
                    )

                    is ScreenType.EditTraining -> EditTrainingScreenContainer(
                        trainingId = screen.trainingId,
                        activity = this@MainActivity,
                        onBackClick = { currentScreen = ScreenType.Training },
                        onNavigate = { currentScreen = it },
                        onStartGameTrainingClick = { gameId ->
                            currentScreen = ScreenType.TrainSingleGame(screen.trainingId, gameId)
                        },
                        inDbProvider = dbProvider,
                    )

                    is ScreenType.TrainSingleGame -> TrainSingleGameLauncherScreenContainer(
                        trainingId = screen.trainingId,
                        gameId = screen.gameId,
                        onTrainingFinished = {
                            currentScreen = ScreenType.EditTraining(screen.trainingId)
                        },
                        onBackClick = {
                            currentScreen = ScreenType.Home
                        },
                        onNavigate = { currentScreen = it },
                        inDbProvider = dbProvider,
                    )

                    ScreenType.GameEditor -> selectedGame?.let { game ->
                        GameEditorScreenContainer(
                            activity = this@MainActivity,
                            game = game,
                            onBackClick = { currentScreen = ScreenType.Home },
                            inDbProvider = dbProvider,
                        )
                    } ?: run {
                        currentScreen = ScreenType.Home
                    }

                    ScreenType.Home -> HomeScreenContainer(
                        activity = this@MainActivity,
                        inDbProvider = dbProvider,
                        simpleViewEnabled = simpleViewEnabled,
                        onNavigate = { currentScreen = it },
                        onCreateTrainingClick = {
                            currentScreen = ScreenType.CreateTraining
                        },
                        onStartFirstTrainingClick = {
                            currentScreen = ScreenType.Training
                        },
                    )

                    ScreenType.Profile -> ProfileScreenContainer(
                        onBackClick = { currentScreen = ScreenType.Home },
                        onNavigate = { currentScreen = it },
                    )

                    ScreenType.Settings -> SettingsScreenContainer(
                        simpleViewEnabled = simpleViewEnabled,
                        onSimpleViewToggle = { simpleViewEnabled = it },
                        onBackClick = { currentScreen = ScreenType.Profile },
                    )

                    else -> currentScreen = ScreenType.Home
                }
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
