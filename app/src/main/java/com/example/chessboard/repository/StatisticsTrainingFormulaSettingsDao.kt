package com.example.chessboard.repository

/*
 * Room DAO for persisted statistics-training formula settings.
 *
 * Keep direct queries for the single settings row here. Do not add formula
 * calculation, validation, or UI behavior to this DAO.
 *
 * Validation date: 2026-05-18
 */

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chessboard.entity.StatisticsTrainingFormulaSettingsEntity

@Dao
interface StatisticsTrainingFormulaSettingsDao {

    @Query("SELECT * FROM statistics_training_formula_settings WHERE id = :id")
    suspend fun getById(
        id: Long = StatisticsTrainingFormulaSettingsEntity.SINGLE_ROW_ID
    ): StatisticsTrainingFormulaSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: StatisticsTrainingFormulaSettingsEntity)
}
