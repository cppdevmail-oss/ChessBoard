package com.example.chessboard.ui.screen.training

/*
 * Unit tests for shared training editor list helpers.
 *
 * Keep pure helper regression tests here. Do not add Compose UI tests or
 * database-backed screen flow tests to this file.
 */

import com.example.chessboard.ui.screen.training.common.TrainingLineEditorItem
import com.example.chessboard.ui.screen.training.common.decreaseTrainingLineWeight
import com.example.chessboard.ui.screen.training.common.increaseTrainingLineWeight
import com.example.chessboard.ui.screen.training.common.removeTrainingLine

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingEditorModelsTest {

    @Test
    fun `decreaseTrainingLineWeight reduces only matching line`() {
        val lines = listOf(
            TrainingLineEditorItem(lineId = 1L, title = "Line 1", weight = 3),
            TrainingLineEditorItem(lineId = 2L, title = "Line 2", weight = 2)
        )

        val updatedLines = decreaseTrainingLineWeight(
            lines = lines,
            lineId = 1L
        )

        assertEquals(2, updatedLines.first { it.lineId == 1L }.weight)
        assertEquals(2, updatedLines.first { it.lineId == 2L }.weight)
    }

    @Test
    fun `decreaseTrainingLineWeight keeps minimum weight at one`() {
        val lines = listOf(
            TrainingLineEditorItem(lineId = 1L, title = "Line 1", weight = 1)
        )

        val updatedLines = decreaseTrainingLineWeight(
            lines = lines,
            lineId = 1L
        )

        assertEquals(1, updatedLines.single().weight)
    }

    @Test
    fun `increaseTrainingLineWeight increases only matching line`() {
        val lines = listOf(
            TrainingLineEditorItem(lineId = 1L, title = "Line 1", weight = 3),
            TrainingLineEditorItem(lineId = 2L, title = "Line 2", weight = 2)
        )

        val updatedLines = increaseTrainingLineWeight(
            lines = lines,
            lineId = 2L
        )

        assertEquals(3, updatedLines.first { it.lineId == 1L }.weight)
        assertEquals(3, updatedLines.first { it.lineId == 2L }.weight)
    }

    @Test
    fun `removeTrainingLine removes only matching line`() {
        val lines = listOf(
            TrainingLineEditorItem(lineId = 1L, title = "Line 1", weight = 3),
            TrainingLineEditorItem(lineId = 2L, title = "Line 2", weight = 2)
        )

        val updatedLines = removeTrainingLine(
            lines = lines,
            lineId = 1L
        )

        assertEquals(listOf(2L), updatedLines.map { it.lineId })
    }

    @Test
    fun `shared helpers keep list unchanged for unknown line id`() {
        val lines = listOf(
            TrainingLineEditorItem(lineId = 1L, title = "Line 1", weight = 3),
            TrainingLineEditorItem(lineId = 2L, title = "Line 2", weight = 2)
        )

        assertEquals(
            lines,
            decreaseTrainingLineWeight(lines = lines, lineId = 99L)
        )
        assertEquals(
            lines,
            increaseTrainingLineWeight(lines = lines, lineId = 99L)
        )
        assertEquals(
            lines,
            removeTrainingLine(lines = lines, lineId = 99L)
        )
    }
}
