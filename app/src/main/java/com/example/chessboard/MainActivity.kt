package com.example.chessboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.ui.TrainingScreen
import com.example.chessboard.ui.theme.ChessBoardTheme

class MainActivity : ComponentActivity() {

    private var gameController = GameController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessBoardTheme {
                TrainingScreen(gameController = gameController)
            }
        }
    }
}