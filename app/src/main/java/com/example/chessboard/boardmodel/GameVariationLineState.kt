package com.example.chessboard.boardmodel

/**
 * Pure state for chess variation lines used by analysis-style screens.
 *
 * Keep UCI-line storage and navigation helpers here. Do not add board rendering,
 * Compose state, database access, or screen navigation logic to this file.
 */
data class GameVariationLineState(
    val lines: List<List<String>> = emptyList(),
    val currentPath: List<String> = emptyList(),
) {
    fun recordPlayedPath(path: List<String>): GameVariationLineState {
        val normalizedPath = path.normalizedUciPath()
        if (normalizedPath.isEmpty()) {
            return copy(currentPath = emptyList())
        }

        val nextLines = normalizeLines(lines + listOf(normalizedPath))
        return copy(
            lines = nextLines,
            currentPath = normalizedPath,
        )
    }

    fun selectPath(path: List<String>): GameVariationLineState {
        return copy(currentPath = path.normalizedUciPath())
    }

    fun backingLineFor(path: List<String> = currentPath): List<String> {
        val normalizedPath = path.normalizedUciPath()
        if (normalizedPath.isEmpty()) {
            return emptyList()
        }

        return lines.firstOrNull { line ->
            line.hasPrefix(normalizedPath)
        } ?: normalizedPath
    }
}

private fun normalizeLines(lines: List<List<String>>): List<List<String>> {
    val normalizedLines = lines
        .map { it.normalizedUciPath() }
        .filter { it.isNotEmpty() }

    return normalizedLines.filterIndexed { index, line ->
        if (normalizedLines.indexOf(line) != index) {
            return@filterIndexed false
        }

        !normalizedLines.any { candidateLine ->
            candidateLine.size > line.size && candidateLine.hasPrefix(line)
        }
    }
}

private fun List<String>.normalizedUciPath(): List<String> {
    return map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}

private fun List<String>.hasPrefix(prefix: List<String>): Boolean {
    if (size < prefix.size) {
        return false
    }

    return take(prefix.size) == prefix
}
