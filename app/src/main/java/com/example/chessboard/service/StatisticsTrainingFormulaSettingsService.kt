package com.example.chessboard.service

/*
 * Loads and persists statistics-training formula settings.
 *
 * Keep default row creation, updates, and reset logic here. Do not add
 * recommendation calculation or screen/UI state.
 *
 * Validation date: 2026-05-18
 */

import com.example.chessboard.entity.StatisticsTrainingFormulaSettingsEntity
import com.example.chessboard.repository.StatisticsTrainingFormulaSettingsDao

class StatisticsTrainingFormulaSettingsService(
    private val dao: StatisticsTrainingFormulaSettingsDao,
) {

    suspend fun getSettings(): StatisticsTrainingFormulaSettingsEntity {
        val existingSettings = dao.getById()
        if (existingSettings != null) {
            return existingSettings
        }

        val defaultSettings = StatisticsTrainingFormulaSettingsEntity()
        dao.upsert(defaultSettings)
        return defaultSettings
    }

    suspend fun updateSettings(settings: StatisticsTrainingFormulaSettingsEntity): StatisticsTrainingFormulaSettingsEntity {
        dao.upsert(settings)
        return settings
    }

    suspend fun resetSettings(): StatisticsTrainingFormulaSettingsEntity {
        val defaultSettings = StatisticsTrainingFormulaSettingsEntity()
        dao.upsert(defaultSettings)
        return defaultSettings
    }
}
