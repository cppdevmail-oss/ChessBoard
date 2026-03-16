package com.example.chessboard

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.example.chessboard.ui.theme.ChessBoardTheme
import com.example.chessboard.ui.ChessBoardWithCoordinates
import com.example.chessboard.boardmodel.GameController

class MainActivity : ComponentActivity() {

    private var gameController = GameController()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessBoardWithCoordinates(gameController)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChessBoardTheme {
        Greeting("Android")
    }
}