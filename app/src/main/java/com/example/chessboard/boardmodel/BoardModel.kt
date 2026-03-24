package com.example.chessboard.boardmodel

enum class PromotionPiece { QUEEN, ROOK, BISHOP, KNIGHT }

data class BoardPiece(
    val letter: Char,
    val field: String
)

data class BoardPosition(
    val pieces: List<BoardPiece>
)

object ChesslibMapper {

    fun fromFen(fen: String): BoardPosition {

        val pieces = mutableListOf<BoardPiece>()
        val boardPart = fen.substringBefore(" ")
        val ranks = boardPart.split("/")

        for (rankIndex in ranks.indices) {

            val rankString = ranks[rankIndex]
            var fileIndex = 0

            for (char in rankString) {
                if (char.isDigit()) {
                    fileIndex += char.digitToInt()
                    continue
                }
                val field = buildFieldName(rankIndex, fileIndex)
                pieces.add(BoardPiece(char, field))
                fileIndex++
            }
        }

        return BoardPosition(pieces)
    }

    private fun buildFieldName(row: Int, col: Int): String {

        val file = ('a' + col)
        val rank = (8 - row)

        return "$file$rank"
    }
}