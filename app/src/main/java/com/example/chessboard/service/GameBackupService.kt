package com.example.chessboard.service

import com.example.chessboard.repository.AppDatabase

class GameBackupService(
    private val database: AppDatabase
) {

    suspend fun getAllGamePgns(): List<String> {
        return database.gameDao().getAllGames().map { game ->
            game.pgn.trim()
        }
    }
}
