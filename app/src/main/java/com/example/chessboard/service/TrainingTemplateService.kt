package com.example.chessboard.service

import com.example.chessboard.entity.TrainingTemplateEntity
import com.example.chessboard.repository.TrainingTemplateDao

class TrainingTemplateService(
    private val dao: TrainingTemplateDao
) {

    enum class UpdateTemplateFromLinesResult {
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

    suspend fun updateTemplateFromLines(
        templateId: Long,
        lines: List<OneLineTrainingData>,
        name: String,
    ): UpdateTemplateFromLinesResult {
        val template = dao.getById(templateId) ?: return UpdateTemplateFromLinesResult.NOT_FOUND
        if (lines.isEmpty()) {
            dao.deleteById(templateId)
            return UpdateTemplateFromLinesResult.DELETED
        }

        dao.update(
            template.copy(
                name = name,
                linesJson = OneLineTrainingData.toJson(lines)
            )
        )
        return UpdateTemplateFromLinesResult.UPDATED
    }

    suspend fun addLine(templateId: Long, lineId: Long, weight: Int): Boolean {
        val template = dao.getById(templateId) ?: return false

        val lines = OneLineTrainingData.fromJson(template.linesJson).toMutableList()
        val index = lines.indexOfFirst { it.lineId == lineId }

        if (index >= 0) {
            lines[index] = lines[index].copy(weight = weight)
        } else {
            lines.add(OneLineTrainingData(lineId, weight))
        }

        dao.update(template.copy(linesJson = OneLineTrainingData.toJson(lines)))
        return true
    }

    suspend fun removeLine(templateId: Long, lineId: Long): Boolean {
        val template = dao.getById(templateId) ?: return false

        val lines = OneLineTrainingData.fromJson(template.linesJson)
            .filter { it.lineId != lineId }

        dao.update(template.copy(linesJson = OneLineTrainingData.toJson(lines)))
        return true
    }

    suspend fun getLines(templateId: Long): List<OneLineTrainingData> {
        val template = dao.getById(templateId) ?: return emptyList()
        return OneLineTrainingData.fromJson(template.linesJson)
    }
}
