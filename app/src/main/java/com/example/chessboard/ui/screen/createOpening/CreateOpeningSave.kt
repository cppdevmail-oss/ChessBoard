package com.example.chessboard.ui.screen.createOpening

/**
 * File role: groups create-opening save planning and persistence orchestration for this screen.
 * Allowed here:
 * - screen-specific save snapshots and results
 * - mapping create-opening state into saved lines and trainings
 * - save helpers for manual, imported single-chapter, and imported multi-chapter flows
 * Not allowed here:
 * - compose UI rendering or launcher setup
 * - reusable app-wide persistence helpers unrelated to create-opening
 * Validation date: 2026-05-05
 */

import com.example.chessboard.entity.LineEntity
import com.example.chessboard.service.LineSaver
import com.example.chessboard.service.OneLineTrainingData
import com.example.chessboard.service.TrainingService
import com.example.chessboard.service.buildStoredPgnFromUci
import com.example.chessboard.service.uciMovesToMoves
import com.example.chessboard.ui.screen.EditableLineSide
import com.github.bhlangonijr.chesslib.move.Move
import com.example.chessboard.repository.DatabaseProvider

internal data class CreateOpeningSaveSnapshot(
    val openingName: String,
    val ecoCode: String,
    val selectedSide: EditableLineSide,
    val importedChapters: List<ImportedChapter>,
    val movesSnapshot: List<Move>,
    val generatedPgn: String,
    val simpleViewEnabled: Boolean,
)

internal sealed interface CreateOpeningSaveResult {
    data object NavigateBack : CreateOpeningSaveResult
    data class OpenPostSaveFlow(
        val state: CreateOpeningPostSaveState,
    ) : CreateOpeningSaveResult
    data class ShowError(
        val message: String,
    ) : CreateOpeningSaveResult
}

internal data class ImportedOpeningLineSavePlan(
    val entity: LineEntity,
    val uciMoves: List<String>,
)

internal suspend fun saveOpening(
    snapshot: CreateOpeningSaveSnapshot,
    dbProvider: DatabaseProvider,
    lineSaver: LineSaver,
    trainingService: TrainingService,
): CreateOpeningSaveResult {
    if (snapshot.importedChapters.size > 1) {
        return saveImportedChaptersAsTrainings(
            snapshot = snapshot,
            lineSaver = lineSaver,
            trainingService = trainingService,
        )
    }

    return saveSingleOpening(
        snapshot = snapshot,
        dbProvider = dbProvider,
        lineSaver = lineSaver,
    )
}

internal fun buildImportedLineSavePlans(
    baseName: String,
    importedChapter: ImportedChapter,
    ecoCode: String,
    selectedSide: EditableLineSide,
): List<ImportedOpeningLineSavePlan> {
    val resolvedEcoCode = resolveImportedEcoCode(
        ecoCode = ecoCode,
        importedChapter = importedChapter,
    )
    val whiteName = importedChapter.headerValue("White") ?: "White"
    val blackName = importedChapter.headerValue("Black") ?: "Black"

    return importedChapter.uciLines.mapIndexed { index, uciMoves ->
        val eventName = buildImportedLineEventName(
            baseName = baseName,
            index = index,
            total = importedChapter.uciLines.size,
        )

        ImportedOpeningLineSavePlan(
            entity = LineEntity(
                white = importedChapter.headerValue("White"),
                black = importedChapter.headerValue("Black"),
                result = importedChapter.headerValue("Result"),
                site = importedChapter.headerValue("Site"),
                round = importedChapter.headerValue("Round"),
                event = eventName,
                eco = resolvedEcoCode,
                pgn = buildStoredPgnFromUci(
                    uciMoves = uciMoves,
                    event = eventName ?: "Opening",
                    whiteName = whiteName,
                    blackName = blackName,
                ),
                initialFen = "",
                sideMask = selectedSide.sideMask,
            ),
            uciMoves = uciMoves,
        )
    }
}

internal fun buildManualOpeningEntity(
    openingName: String,
    ecoCode: String,
    generatedPgn: String,
    selectedSide: EditableLineSide,
): LineEntity {
    return LineEntity(
        event = openingName.ifBlank { null },
        eco = ecoCode.ifBlank { null },
        pgn = generatedPgn,
        initialFen = "",
        sideMask = selectedSide.sideMask,
    )
}

private suspend fun saveImportedChaptersAsTrainings(
    snapshot: CreateOpeningSaveSnapshot,
    lineSaver: LineSaver,
    trainingService: TrainingService,
): CreateOpeningSaveResult {
    var savedChaptersCount = 0

    snapshot.importedChapters.forEachIndexed { chapterIndex, importedChapter ->
        val chapterName = resolveImportedChapterSaveName(
            importedChapter = importedChapter,
            fallbackOpeningName = snapshot.openingName,
            chapterIndex = chapterIndex,
        )
        val savePlans = buildImportedLineSavePlans(
            baseName = chapterName,
            importedChapter = importedChapter,
            ecoCode = snapshot.ecoCode,
            selectedSide = snapshot.selectedSide,
        )
        val savedLineIds = saveImportedLinePlans(
            lineSaver = lineSaver,
            savePlans = savePlans,
        )

        if (savedLineIds.isEmpty()) {
            return@forEachIndexed
        }

        trainingService.createTrainingFromLines(
            name = chapterName,
            lines = savedLineIds.map { lineId ->
                OneLineTrainingData(lineId = lineId, weight = 1)
            },
        )
        savedChaptersCount++
    }

    if (savedChaptersCount == 0) {
        return CreateOpeningSaveResult.ShowError("None of the imported chapters could be saved")
    }

    return CreateOpeningSaveResult.NavigateBack
}

private suspend fun saveSingleOpening(
    snapshot: CreateOpeningSaveSnapshot,
    dbProvider: DatabaseProvider,
    lineSaver: LineSaver,
): CreateOpeningSaveResult {
    val firstChapter = snapshot.importedChapters.firstOrNull()
    if (firstChapter == null) {
        return saveManualOpening(
            snapshot = snapshot,
            dbProvider = dbProvider,
            lineSaver = lineSaver,
        )
    }

    val savedLineIds = saveImportedLinePlans(
        lineSaver = lineSaver,
        savePlans = buildImportedLineSavePlans(
            baseName = snapshot.openingName,
            importedChapter = firstChapter,
            ecoCode = snapshot.ecoCode,
            selectedSide = snapshot.selectedSide,
        ),
    )
    if (savedLineIds.isEmpty()) {
        return CreateOpeningSaveResult.ShowError("None of the imported lines could be saved")
    }

    if (snapshot.simpleViewEnabled) {
        createOpeningTraining(
            dbProvider = dbProvider,
            savedLines = SavedOpeningLines(
                name = snapshot.openingName.ifBlank { "Opening" },
                lineIds = savedLineIds,
            ),
        )
        return CreateOpeningSaveResult.NavigateBack
    }

    return CreateOpeningSaveResult.OpenPostSaveFlow(
        state = startCreateOpeningPostSaveFlow(
            openingName = snapshot.openingName,
            savedLineIds = savedLineIds,
        ),
    )
}

private suspend fun saveManualOpening(
    snapshot: CreateOpeningSaveSnapshot,
    dbProvider: DatabaseProvider,
    lineSaver: LineSaver,
): CreateOpeningSaveResult {
    val entity = buildManualOpeningEntity(
        openingName = snapshot.openingName,
        ecoCode = snapshot.ecoCode,
        generatedPgn = snapshot.generatedPgn,
        selectedSide = snapshot.selectedSide,
    )
    val savedLineId = lineSaver.saveLine(
        line = entity,
        moves = snapshot.movesSnapshot,
        sideMask = entity.sideMask,
    )
    if (savedLineId == null) {
        return CreateOpeningSaveResult.ShowError("Failed to save opening")
    }

    if (snapshot.simpleViewEnabled) {
        createOpeningTraining(
            dbProvider = dbProvider,
            savedLines = SavedOpeningLines(
                name = snapshot.openingName.ifBlank { "Opening" },
                lineIds = listOf(savedLineId),
            ),
        )
        return CreateOpeningSaveResult.NavigateBack
    }

    return CreateOpeningSaveResult.OpenPostSaveFlow(
        state = startCreateOpeningPostSaveFlow(
            openingName = snapshot.openingName,
            savedLineIds = listOf(savedLineId),
        ),
    )
}

private suspend fun saveImportedLinePlans(
    lineSaver: LineSaver,
    savePlans: List<ImportedOpeningLineSavePlan>,
): List<Long> {
    val savedLineIds = mutableListOf<Long>()

    for (savePlan in savePlans) {
        val savedId = lineSaver.saveOrGetExistingLineId(
            line = savePlan.entity,
            moves = uciMovesToMoves(savePlan.uciMoves),
            sideMask = savePlan.entity.sideMask,
        )
        if (savedId != null) {
            savedLineIds.add(savedId)
        }
    }

    return savedLineIds
}

private fun resolveImportedChapterSaveName(
    importedChapter: ImportedChapter,
    fallbackOpeningName: String,
    chapterIndex: Int,
): String {
    val importedChapterName = importedChapter.resolveChapterName()
    if (importedChapterName != null) {
        return importedChapterName
    }

    if (fallbackOpeningName.isNotBlank()) {
        return fallbackOpeningName
    }

    return "Chapter ${chapterIndex + 1}"
}

private fun resolveImportedEcoCode(
    ecoCode: String,
    importedChapter: ImportedChapter,
): String? {
    if (ecoCode.isNotBlank()) {
        return ecoCode
    }

    return importedChapter.headerValue("ECO")
}
