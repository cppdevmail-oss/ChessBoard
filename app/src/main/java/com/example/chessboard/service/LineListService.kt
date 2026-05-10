package com.example.chessboard.service

import com.example.chessboard.entity.LineEntity
import com.example.chessboard.repository.LineDao

class LineListService(
    private val lineDao: LineDao
) {

    suspend fun getLinesPage(limit: Int, offset: Int): List<LineEntity> {
        val normalizedLimit = normalizeLimit(limit)
        val normalizedOffset = normalizeOffset(offset)
        return lineDao.getLinesPage(limit = normalizedLimit, offset = normalizedOffset)
    }

    suspend fun getLineIdsPage(limit: Int, offset: Int): List<Long> {
        val normalizedLimit = normalizeLimit(limit)
        val normalizedOffset = normalizeOffset(offset)
        return lineDao.getLineIdsPage(limit = normalizedLimit, offset = normalizedOffset)
    }

    suspend fun getLinesCount(): Int {
        return lineDao.getLinesCount()
    }

    suspend fun getLinesByIds(lineIds: List<Long>): List<LineEntity> {
        if (lineIds.isEmpty()) {
            return emptyList()
        }

        val distinctLineIds = lineIds.distinct()
        val linesById = lineDao.getByIds(distinctLineIds)
            .associateBy { line -> line.id }

        return lineIds.mapNotNull { lineId -> linesById[lineId] }
    }

    suspend fun searchLinesByName(
        query: String,
        isCaseSensitive: Boolean,
        limit: Int,
        offset: Int
    ): List<LineEntity> {
        if (query.isBlank()) {
            return getLinesPage(limit = limit, offset = offset)
        }

        val normalizedLimit = normalizeLimit(limit)
        val normalizedOffset = normalizeOffset(offset)
        if (isCaseSensitive) {
            return lineDao.searchLinesByEventCaseSensitive(
                query = query,
                limit = normalizedLimit,
                offset = normalizedOffset
            )
        }

        return lineDao.searchLinesByEvent(
            query = query,
            limit = normalizedLimit,
            offset = normalizedOffset
        )
    }

    suspend fun searchLineIdsByName(
        query: String,
        isCaseSensitive: Boolean,
        limit: Int,
        offset: Int
    ): List<Long> {
        if (query.isBlank()) {
            return getLineIdsPage(limit = limit, offset = offset)
        }

        val normalizedLimit = normalizeLimit(limit)
        val normalizedOffset = normalizeOffset(offset)
        if (isCaseSensitive) {
            return lineDao.searchLineIdsByEventCaseSensitive(
                query = query,
                limit = normalizedLimit,
                offset = normalizedOffset
            )
        }

        return lineDao.searchLineIdsByEvent(
            query = query,
            limit = normalizedLimit,
            offset = normalizedOffset
        )
    }

    suspend fun countLinesByName(
        query: String,
        isCaseSensitive: Boolean
    ): Int {
        if (query.isBlank()) {
            return getLinesCount()
        }

        if (isCaseSensitive) {
            return lineDao.countLinesByEventCaseSensitive(query)
        }

        return lineDao.countLinesByEvent(query)
    }

    private fun normalizeLimit(limit: Int): Int {
        if (limit <= 0) {
            return 1
        }

        return limit
    }

    private fun normalizeOffset(offset: Int): Int {
        if (offset < 0) {
            return 0
        }

        return offset
    }
}
