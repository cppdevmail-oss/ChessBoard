package com.example.chessboard.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OneLineTrainingData(
    @SerialName("gameId")
    val lineId: Long,
    val weight: Int
) {
    companion object {
        fun fromJson(jsonString: String): List<OneLineTrainingData> {
            return try {
                Json.decodeFromString(jsonString)
            } catch (e: Exception) {
                val method = Throwable().stackTrace[1].methodName
                println("Error [${e}] on [${method}]")
                emptyList()
            }
        }

        fun toJson(lines: List<OneLineTrainingData>): String {
            return Json.encodeToString(lines)
        }
    }
}
