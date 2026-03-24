package com.example.chessboard

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*

import com.example.chessboard.entity.GameEntity
import com.example.chessboard.ui.theme.ChessBoardTheme
import com.example.chessboard.ui.screen.CreateOpeningScreenContainer
import com.example.chessboard.ui.screen.GameEditorScreenContainer
import com.example.chessboard.ui.screen.HomeScreenContainer
import com.example.chessboard.ui.screen.TrainingScreenContainer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ChessBoardTheme {
                var currentScreen by remember { mutableStateOf("Home") }
                var selectedGame by remember { mutableStateOf<GameEntity?>(null) }

                when (currentScreen) {
                    "Training" -> TrainingScreenContainer(
                        activity = this@MainActivity,
                        onBackClick = { currentScreen = "Home" },
                        onNavigate = { currentScreen = it }
                    )
                    "CreateOpening" -> CreateOpeningScreenContainer(
                        activity = this@MainActivity,
                        onBackClick = { currentScreen = "Home" }
                    )
                    "GameEditor" -> selectedGame?.let { game ->
                        GameEditorScreenContainer(
                            activity = this@MainActivity,
                            game = game,
                            onBackClick = { currentScreen = "Home" }
                        )
                    } ?: run { currentScreen = "Home" }
                    else -> HomeScreenContainer(
                        activity = this@MainActivity,
                        onNavigate = { currentScreen = it },
                        onOpenGame = { game ->
                            selectedGame = game
                            currentScreen = "GameEditor"
                        }
                    )
                }
            }
        }
    }
}
