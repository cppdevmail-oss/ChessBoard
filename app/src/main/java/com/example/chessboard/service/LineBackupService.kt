package com.example.chessboard.service

import com.example.chessboard.entity.LineEntity
import com.example.chessboard.entity.SideMask
import com.example.chessboard.repository.AppDatabase
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class LineBackupRestoreResult(
    val restoredLinesCount: Int,
    val skippedLinesCount: Int
)

data class LineBackupRestoreProgress(
    val totalLines: Int,
    val processedLinesCount: Int,
    val restoredLinesCount: Int,
    val skippedLinesCount: Int,
)

class LineBackupService(
    private val database: AppDatabase
) {

    suspend fun getAllLinePgns(): List<String> {
        return database.lineDao().getAllLines().map { line ->
            buildBackupPgn(line)
        }
    }

    suspend fun writeBackup(outputStream: OutputStream) {
        val backupText = getAllLinePgns().joinToString(separator = "\n\n") { pgn ->
            pgn.trim()
        }
        outputStream.writer(Charsets.UTF_8).use { writer ->
            writer.write(backupText)
        }
    }

    suspend fun restoreBackup(
        inputStream: InputStream,
        onProgress: suspend (LineBackupRestoreProgress) -> Unit = {}
    ): LineBackupRestoreResult {
        val backupText = inputStream.reader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
        val backupLines = extractBackupLines(backupText)
        if (backupLines.isEmpty()) {
            onProgress(
                LineBackupRestoreProgress(
                    totalLines = 0,
                    processedLinesCount = 0,
                    restoredLinesCount = 0,
                    skippedLinesCount = 0
                )
            )
            return LineBackupRestoreResult(
                restoredLinesCount = 0,
                skippedLinesCount = 0
            )
        }

        val lineSaver = LineSaver(database)
        var restoredLinesCount = 0
        var skippedLinesCount = 0
        var processedLinesCount = 0

        suspend fun reportProgress() {
            onProgress(
                LineBackupRestoreProgress(
                    totalLines = backupLines.size,
                    processedLinesCount = processedLinesCount,
                    restoredLinesCount = restoredLinesCount,
                    skippedLinesCount = skippedLinesCount
                )
            )
        }

        reportProgress()

        backupLines.forEach { pgn ->
            currentCoroutineContext().ensureActive()

            val line = buildBackupLineEntity(pgn)
            val moves = runCatching {
                uciMovesToMoves(extractBackupMoves(pgn))
            }.getOrNull()

            if (moves == null) {
                skippedLinesCount += 1
                processedLinesCount += 1
                reportProgress()
                return@forEach
            }

            val lineId = lineSaver.saveLine(line, moves, line.sideMask)
            if (lineId == null) {
                skippedLinesCount += 1
                processedLinesCount += 1
                reportProgress()
                return@forEach
            }

            restoredLinesCount += 1
            processedLinesCount += 1
            reportProgress()
        }

        return LineBackupRestoreResult(
            restoredLinesCount = restoredLinesCount,
            skippedLinesCount = skippedLinesCount
        )
    }

    private fun buildBackupPgn(line: LineEntity): String {
        val normalizedPgn = line.pgn.trim()
        val lines = normalizedPgn.lines().toMutableList()
        val headerEndIndex = lines.indexOfFirst { it.isBlank() }
        val insertIndex = if (headerEndIndex >= 0) {
            headerEndIndex
        } else {
            lines.size
        }

        replaceHeaderOrInsert(
            lines = lines,
            headerName = "SideMask",
            headerValue = "[SideMask \"${line.sideMask}\"]",
            insertIndex = insertIndex
        )

        if (line.initialFen.isNotBlank()) {
            replaceHeaderOrInsert(
                lines = lines,
                headerName = "FEN",
                headerValue = "[FEN \"${line.initialFen}\"]",
                insertIndex = insertIndex
            )
            replaceHeaderOrInsert(
                lines = lines,
                headerName = "SetUp",
                headerValue = "[SetUp \"1\"]",
                insertIndex = insertIndex
            )
        }

        return lines.joinToString(separator = "\n").trim()
    }

    private fun replaceHeaderOrInsert(
        lines: MutableList<String>,
        headerName: String,
        headerValue: String,
        insertIndex: Int
    ) {
        val headerPrefix = "[$headerName "
        val existingIndex = lines.indexOfFirst { line ->
            line.startsWith(headerPrefix)
        }
        if (existingIndex >= 0) {
            lines[existingIndex] = headerValue
            return
        }

        lines.add(insertIndex, headerValue)
    }

    private fun extractBackupLines(backupText: String): List<String> {
        val normalizedText = backupText.trim()
        if (normalizedText.isBlank()) {
            return emptyList()
        }

        val lineStartRegex = Regex("""(?m)(?=^\[Event\s+")""")
        val splitLines = normalizedText
            .split(lineStartRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (splitLines.isNotEmpty()) {
            return splitLines
        }

        return listOf(normalizedText)
    }

    private fun buildBackupLineEntity(pgn: String): LineEntity {
        val headers = extractPgnHeaders(pgn)

        fun normalizeHeaderValue(value: String?): String? {
            val trimmed = value?.trim() ?: return null
            if (trimmed.isBlank() || trimmed == "?") {
                return null
            }

            return trimmed
        }

        return LineEntity(
            white = normalizeHeaderValue(headers["White"]),
            black = normalizeHeaderValue(headers["Black"]),
            result = normalizeHeaderValue(headers["Result"]),
            event = normalizeHeaderValue(headers["Event"]),
            site = normalizeHeaderValue(headers["Site"]),
            round = normalizeHeaderValue(headers["Round"]),
            eco = normalizeHeaderValue(headers["ECO"]),
            pgn = pgn.trim(),
            initialFen = normalizeHeaderValue(headers["FEN"]) ?: "",
            sideMask = resolveBackupSideMask(headers)
        )
    }

    private fun resolveBackupSideMask(headers: Map<String, String>): Int {
        val rawSideMask = headers["SideMask"]?.trim()?.toIntOrNull() ?: return SideMask.WHITE
        if (rawSideMask == SideMask.WHITE || rawSideMask == SideMask.BLACK || rawSideMask == SideMask.BOTH) {
            return rawSideMask
        }

        return SideMask.WHITE
    }

    private fun extractBackupMoves(pgn: String): List<String> {
        val uciRegex = Regex("[a-h][1-8][a-h][1-8][qrbnQRBN]?")
        return pgn.lines()
            .filterNot { it.trim().startsWith("[") }
            .joinToString(" ")
            .split("\\s+".toRegex())
            .filter { uciRegex.matches(it) }
    }
}
