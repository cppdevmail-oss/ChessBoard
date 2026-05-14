package com.example.chessboard.ui.screen.createOpening

/**
 * File role: groups create-opening save runtime state and small save-runtime helpers.
 * Allowed here:
 * - runtime-only state used by the create-opening container while saving
 * - small helpers for formatting save runtime messages
 * - test seam types for replacing the save runner
 * Not allowed here:
 * - Compose UI rendering, launcher setup, or database save implementation
 */
import com.example.chessboard.repository.DatabaseProvider
import com.example.chessboard.service.LineSaver
import com.example.chessboard.service.TrainingService
import kotlinx.coroutines.Job

internal typealias CreateOpeningSaveRunner = suspend (
    snapshot: CreateOpeningSaveSnapshot,
    dbProvider: DatabaseProvider,
    lineSaver: LineSaver,
    trainingService: TrainingService,
    onProgress: suspend (CreateOpeningSaveProgress) -> Unit,
) -> CreateOpeningSaveResult

internal data class CreateOpeningSaveRuntimeState(
    val message: String? = null,
    val progress: CreateOpeningSaveProgress? = null,
    val job: Job? = null,
)

internal fun resolveCreateOpeningSaveCanceledMessage(
    progress: CreateOpeningSaveProgress?,
): String {
    val currentProgress = progress ?: return "Save canceled."

    return buildString {
        appendLine("Save canceled.")
        appendLine("Processed lines: ${currentProgress.processedLinesCount}/${currentProgress.totalLines}")
        appendLine("Saved lines: ${currentProgress.savedLinesCount}")
        append("Skipped lines: ${currentProgress.skippedLinesCount}")
    }
}
