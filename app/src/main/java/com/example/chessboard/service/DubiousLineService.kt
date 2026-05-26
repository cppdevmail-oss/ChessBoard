package com.example.chessboard.service

/*
 * File role: coordinates persistence operations for optional dubious-line markers.
 * Allowed here:
 * - default marker creation rules backed by the dubious_lines table
 * - simple read helpers for persisted dubious-line records
 * Not allowed here:
 * - UI state, screen workflow, or training recommendation formulas
 * Validation date: 2026-05-25
 */

import com.example.chessboard.entity.DubiousLineEntity
import com.example.chessboard.repository.DubiousLineDao

private const val DEFAULT_DUBIOUS_LINE_WEIGHT = 1

class DubiousLineService(
    private val dao: DubiousLineDao,
) {
    suspend fun markDubious(lineId: Long) {
        dao.insertIfAbsent(
            DubiousLineEntity(
                lineId = lineId,
                weight = DEFAULT_DUBIOUS_LINE_WEIGHT,
            ),
        )
    }

    suspend fun getAll(): List<DubiousLineEntity> = dao.getAll()

    suspend fun isDubious(lineId: Long): Boolean = dao.getByLineId(lineId) != null

    suspend fun delete(lineId: Long) {
        dao.deleteByLineId(lineId)
    }
}
