package com.example.chessboard.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chessboard.entity.LineEntity

@Dao
interface LineDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLine(line: LineEntity): Long

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM games ORDER BY id DESC")
    suspend fun getAllLines(): List<LineEntity>

    @Query("SELECT * FROM games ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getLinesPage(limit: Int, offset: Int): List<LineEntity>

    @Query("SELECT id FROM games ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getLineIdsPage(limit: Int, offset: Int): List<Long>

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getLinesCount(): Int

    @Query(
        """
        SELECT * FROM games
        WHERE instr(lower(ifnull(event, '')), lower(:query)) > 0
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun searchLinesByEvent(query: String, limit: Int, offset: Int): List<LineEntity>

    @Query(
        """
        SELECT id FROM games
        WHERE instr(lower(ifnull(event, '')), lower(:query)) > 0
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun searchLineIdsByEvent(query: String, limit: Int, offset: Int): List<Long>

    @Query(
        """
        SELECT COUNT(*) FROM games
        WHERE instr(lower(ifnull(event, '')), lower(:query)) > 0
        """
    )
    suspend fun countLinesByEvent(query: String): Int

    @Query(
        """
        SELECT * FROM games
        WHERE instr(ifnull(event, ''), :query) > 0
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun searchLinesByEventCaseSensitive(
        query: String,
        limit: Int,
        offset: Int
    ): List<LineEntity>

    @Query(
        """
        SELECT id FROM games
        WHERE instr(ifnull(event, ''), :query) > 0
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun searchLineIdsByEventCaseSensitive(
        query: String,
        limit: Int,
        offset: Int
    ): List<Long>

    @Query(
        """
        SELECT COUNT(*) FROM games
        WHERE instr(ifnull(event, ''), :query) > 0
        """
    )
    suspend fun countLinesByEventCaseSensitive(query: String): Int

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): LineEntity?

    @Query("SELECT * FROM games WHERE id IN (:lineIds)")
    suspend fun getByIds(lineIds: List<Long>): List<LineEntity>
}
