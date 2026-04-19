package com.example.chessboard.service

import com.example.chessboard.entity.TrainingTemplateEntity
import com.example.chessboard.repository.TrainingTemplateDao

class TrainingTemplateService(
    private val dao: TrainingTemplateDao
) {

    enum class UpdateTemplateFromGamesResult {
        UPDATED,
        DELETED,
        NOT_FOUND,
    }

    suspend fun getAllTemplates(): List<TrainingTemplateEntity> {
        return dao.getAll()
    }

    suspend fun getTemplateById(templateId: Long): TrainingTemplateEntity? {
        return dao.getById(templateId)
    }

    suspend fun deleteTemplate(templateId: Long) {
        dao.deleteById(templateId)
    }

    suspend fun createTemplate(name: String): Long {
        return dao.insert(TrainingTemplateEntity(name = name))
    }

    suspend fun updateTemplateFromGames(
        templateId: Long,
        games: List<OneGameTrainingData>,
        name: String,
    ): UpdateTemplateFromGamesResult {
        val template = dao.getById(templateId) ?: return UpdateTemplateFromGamesResult.NOT_FOUND
        if (games.isEmpty()) {
            dao.deleteById(templateId)
            return UpdateTemplateFromGamesResult.DELETED
        }

        dao.update(
            template.copy(
                name = name,
                gamesJson = OneGameTrainingData.toJson(games)
            )
        )
        return UpdateTemplateFromGamesResult.UPDATED
    }

    suspend fun addGame(templateId: Long, gameId: Long, weight: Int): Boolean {
        val template = dao.getById(templateId) ?: return false

        val games = OneGameTrainingData.fromJson(template.gamesJson).toMutableList()
        val index = games.indexOfFirst { it.gameId == gameId }

        if (index >= 0) {
            games[index] = games[index].copy(weight = weight)
        } else {
            games.add(OneGameTrainingData(gameId, weight))
        }

        dao.update(template.copy(gamesJson = OneGameTrainingData.toJson(games)))
        return true
    }

    suspend fun removeGame(templateId: Long, gameId: Long): Boolean {
        val template = dao.getById(templateId) ?: return false

        val games = OneGameTrainingData.fromJson(template.gamesJson)
            .filter { it.gameId != gameId }

        dao.update(template.copy(gamesJson = OneGameTrainingData.toJson(games)))
        return true
    }

    suspend fun getGames(templateId: Long): List<OneGameTrainingData> {
        val template = dao.getById(templateId) ?: return emptyList()
        return OneGameTrainingData.fromJson(template.gamesJson)
    }
}
