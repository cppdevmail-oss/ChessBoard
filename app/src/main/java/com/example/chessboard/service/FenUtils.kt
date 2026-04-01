package com.example.chessboard.service

fun normalizeFenWithoutMoveNumbers(
    fen: String,
    includeEnPassant: Boolean = false
): String {
    val fenParts = fen.trim().split(Regex("\\s+"))

    val normalizedPartsCount = resolveNormalizedFenPartsCount(includeEnPassant)
    if (fenParts.size <= normalizedPartsCount) {
        return fen.trim()
    }

    return fenParts.take(normalizedPartsCount).joinToString(separator = " ")
}

fun calculateFenHashWithoutMoveNumbers(
    fen: String,
    includeEnPassant: Boolean = false
): Long {
    return normalizeFenWithoutMoveNumbers(
        fen = fen,
        includeEnPassant = includeEnPassant
    ).hashCode().toLong()
}

private fun resolveNormalizedFenPartsCount(includeEnPassant: Boolean): Int {
    if (includeEnPassant) {
        return 4
    }

    return 3
}
