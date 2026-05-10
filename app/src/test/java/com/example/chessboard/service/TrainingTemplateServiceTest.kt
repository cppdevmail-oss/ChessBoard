package com.example.chessboard.service

/*
 * Unit tests for TrainingTemplateService.
 *
 * Keep service-level persistence contract tests here using fake DAOs. Do not
 * add Room integration tests or UI-driven flows to this file.
 */

import com.example.chessboard.entity.TrainingTemplateEntity
import com.example.chessboard.repository.TrainingTemplateDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingTemplateServiceTest {

    @Test
    fun `updateTemplateFromLines returns not found when template is missing`() = runBlocking {
        val dao = FakeTrainingTemplateDao()
        val service = TrainingTemplateService(dao)

        val updateResult = service.updateTemplateFromLines(
            templateId = 1L,
            lines = listOf(OneLineTrainingData(lineId = 10L, weight = 2)),
            name = "Updated Template",
        )

        assertEquals(TrainingTemplateService.UpdateTemplateFromLinesResult.NOT_FOUND, updateResult)
    }

    @Test
    fun `updateTemplateFromLines updates template name and lines json`() = runBlocking {
        val dao = FakeTrainingTemplateDao(
            templates = mutableMapOf(
                1L to TrainingTemplateEntity(
                    id = 1L,
                    name = "Initial Template",
                    linesJson = "[]",
                )
            )
        )
        val service = TrainingTemplateService(dao)
        val updatedLines = listOf(
            OneLineTrainingData(lineId = 10L, weight = 2),
            OneLineTrainingData(lineId = 20L, weight = 1),
        )

        val updateResult = service.updateTemplateFromLines(
            templateId = 1L,
            lines = updatedLines,
            name = "Updated Template",
        )

        assertEquals(TrainingTemplateService.UpdateTemplateFromLinesResult.UPDATED, updateResult)
        assertEquals("Updated Template", dao.templates.getValue(1L).name)
        assertEquals(
            OneLineTrainingData.toJson(updatedLines),
            dao.templates.getValue(1L).linesJson,
        )
    }

    @Test
    fun `updateTemplateFromLines deletes template when lines list is empty`() = runBlocking {
        val dao = FakeTrainingTemplateDao(
            templates = mutableMapOf(
                1L to TrainingTemplateEntity(
                    id = 1L,
                    name = "Initial Template",
                    linesJson = OneLineTrainingData.toJson(
                        listOf(OneLineTrainingData(lineId = 10L, weight = 2))
                    ),
                )
            )
        )
        val service = TrainingTemplateService(dao)

        val updateResult = service.updateTemplateFromLines(
            templateId = 1L,
            lines = emptyList(),
            name = "Empty Template",
        )

        assertEquals(TrainingTemplateService.UpdateTemplateFromLinesResult.DELETED, updateResult)
        assertNull(dao.templates[1L])
    }

    private class FakeTrainingTemplateDao(
        val templates: MutableMap<Long, TrainingTemplateEntity> = mutableMapOf(),
    ) : TrainingTemplateDao {
        override suspend fun insert(template: TrainingTemplateEntity): Long {
            templates[template.id] = template
            return template.id
        }

        override suspend fun getAll(): List<TrainingTemplateEntity> {
            return templates.values.toList()
        }

        override suspend fun getById(id: Long): TrainingTemplateEntity? {
            return templates[id]
        }

        override suspend fun deleteById(id: Long) {
            templates.remove(id)
        }

        override suspend fun update(template: TrainingTemplateEntity) {
            templates[template.id] = template
        }
    }
}
