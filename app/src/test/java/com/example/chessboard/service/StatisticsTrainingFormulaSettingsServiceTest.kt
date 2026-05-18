package com.example.chessboard.service

/*
 * Unit tests for persisted statistics-training formula settings mapping.
 *
 * Keep service-level default, update, and reset coverage here using a fake DAO.
 * Do not add Room integration tests or recommendation formula tests here.
 *
 * Validation date: 2026-05-18
 */

import com.example.chessboard.entity.StatisticsTrainingFormulaSettingsEntity
import com.example.chessboard.repository.StatisticsTrainingFormulaSettingsDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StatisticsTrainingFormulaSettingsServiceTest {

    @Test
    fun `getSettings creates default row when settings are missing`() = runBlocking {
        val dao = FakeStatisticsTrainingFormulaSettingsDao()
        val service = StatisticsTrainingFormulaSettingsService(dao)

        val settings = service.getSettings()

        assertEquals(StatisticsTrainingFormulaSettingsEntity(), settings)
        assertNotNull(dao.settings)
        assertEquals(StatisticsTrainingFormulaSettingsEntity.SINGLE_ROW_ID, dao.settings?.id)
    }

    @Test
    fun `updateSettings persists formula settings`() = runBlocking {
        val dao = FakeStatisticsTrainingFormulaSettingsDao()
        val service = StatisticsTrainingFormulaSettingsService(dao)
        val updatedSettings = StatisticsTrainingFormulaSettingsEntity(
            recentResultsPerLine = 3,
            recencyDaysCap = 10,
            lastMistakeWeight = 6.0,
            maxMistakesLast = 4,
            avgMistakesWeight = 1.5,
            maxAvgMistakesRecent = 2.5,
            recencyWeight = 1.0,
            perfectRatePenaltyWeight = 4.0,
            noAttemptsBoost = 5.0,
            oneAttemptBoost = 3.0,
            twoAttemptsBoost = 2.0,
            weight5ScoreThreshold = 12.0,
            weight4ScoreThreshold = 8.0,
            weight3ScoreThreshold = 5.0,
            weight2ScoreThreshold = 3.0,
        )

        service.updateSettings(updatedSettings)

        assertEquals(updatedSettings, service.getSettings())
    }

    @Test
    fun `resetSettings restores default formula settings`() = runBlocking {
        val dao = FakeStatisticsTrainingFormulaSettingsDao()
        val service = StatisticsTrainingFormulaSettingsService(dao)
        service.updateSettings(StatisticsTrainingFormulaSettingsEntity(lastMistakeWeight = 9.0))

        val resetSettings = service.resetSettings()

        assertEquals(StatisticsTrainingFormulaSettingsEntity(), resetSettings)
        assertEquals(StatisticsTrainingFormulaSettingsEntity(), service.getSettings())
    }

    private class FakeStatisticsTrainingFormulaSettingsDao(
        var settings: StatisticsTrainingFormulaSettingsEntity? = null,
    ) : StatisticsTrainingFormulaSettingsDao {
        override suspend fun getById(id: Long): StatisticsTrainingFormulaSettingsEntity? {
            return settings?.takeIf { existingSettings -> existingSettings.id == id }
        }

        override suspend fun upsert(settings: StatisticsTrainingFormulaSettingsEntity) {
            this.settings = settings
        }
    }
}
