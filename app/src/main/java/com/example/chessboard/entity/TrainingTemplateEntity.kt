package com.example.chessboard.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * Training template.
 *
 * linesJson format:
 * [
 *   {"lineId": 1, "weight": 3},
 *   {"lineId": 5, "weight": 1}
 * ]
 */
@Entity(tableName = "training_templates")
data class TrainingTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "gamesJson")
    val linesJson: String = "[]"
)
