package com.example.chessboard.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chessboard.entity.LinePositionEntity

data class PositionUsage(
    val positionId: Long,
    val sideMask: Int
)

@Dao
interface LinePositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinePosition(linePosition: LinePositionEntity): Long

    @Query("DELETE FROM game_positions WHERE gameId = :lineId")
    suspend fun deleteByLineId(lineId: Long)

    @Query("""
        SELECT positionId, sideMask
        FROM game_positions
        WHERE gameId = :lineId""")
    suspend fun getPositionsForLine(lineId: Long): List<PositionUsage>

    @Query("""
        SELECT gp.positionId, g.sideMask as sideMask
        FROM game_positions gp
        JOIN games g ON g.id = gp.gameId
        WHERE gp.positionId = :positionId""")
    suspend fun getUsage(positionId: Long): List<PositionUsage>

    @Query("""
        SELECT DISTINCT gameId
        FROM game_positions
        WHERE positionId IN (:positionIds)
        ORDER BY gameId
    """)
    suspend fun getLineIdsByPositionIds(positionIds: List<Long>): List<Long>

    @Query("""
        SELECT gameId FROM game_positions
        WHERE positionId = :positionId AND ply = :ply
        LIMIT 1
    """)
    suspend fun getLineIdByPositionAndPly(positionId: Long, ply: Int): Long?
}
