package com.example.chessboard.ui.screen.createOpening

/**
 * File role: groups unit tests for create-opening import helpers.
 * Allowed here:
 * - pure tests for imported chapter metadata resolution and draft autofill behavior
 * - assertions about screen-specific import parsing helpers that do not require Compose or Android UI
 * Not allowed here:
 * - database-backed save integration tests
 * - compose rendering tests for the create-opening screen
 * Validation date: 2026-05-05
 */

import com.example.chessboard.boardmodel.LineDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreateOpeningImportTest {

    @Test
    fun `resolveChapterName prefers chapter name then event then study name`() {
        val chapterNameFirst = importedChapter(
            headers = mapOf(
                "ChapterName" to "Main Line",
                "Event" to "Ignored Event",
                "StudyName" to "Ignored Study",
            ),
        )
        val eventFallback = importedChapter(
            headers = mapOf(
                "ChapterName" to "?",
                "Event" to "French Defense",
                "StudyName" to "Fallback Study",
            ),
        )
        val studyFallback = importedChapter(
            headers = mapOf(
                "ChapterName" to " ",
                "Event" to "?",
                "StudyName" to "Study Chapter",
            ),
        )
        val noMeaningfulName = importedChapter(
            headers = mapOf(
                "ChapterName" to "",
                "Event" to "?",
                "StudyName" to " ",
            ),
        )

        assertEquals("Main Line", chapterNameFirst.resolveChapterName())
        assertEquals("French Defense", eventFallback.resolveChapterName())
        assertEquals("Study Chapter", studyFallback.resolveChapterName())
        assertNull(noMeaningfulName.resolveChapterName())
    }

    @Test
    fun `applyImportedChapterToDraft updates only meaningful imported fields`() {
        val draft = LineDraft()
        val importedChapter = importedChapter(
            headers = mapOf(
                "Event" to "Caro-Kann Defense",
                "ECO" to "B12",
            ),
        )

        val updatedDraft = applyImportedChapterToDraft(
            lineDraft = draft,
            importedChapter = importedChapter,
        )

        assertEquals("Caro-Kann Defense", updatedDraft.line.event)
        assertEquals("B12", updatedDraft.line.eco)
    }

    @Test
    fun `applyImportedChapterToDraft keeps existing values when imported headers are not meaningful`() {
        val draft = LineDraft(
            line = LineDraft().line.copy(
                event = "Existing Opening",
                eco = "A00",
            ),
        )
        val importedChapter = importedChapter(
            headers = mapOf(
                "Event" to "?",
                "ECO" to " ",
            ),
        )

        val updatedDraft = applyImportedChapterToDraft(
            lineDraft = draft,
            importedChapter = importedChapter,
        )

        assertEquals("Existing Opening", updatedDraft.line.event)
        assertEquals("A00", updatedDraft.line.eco)
    }

    private fun importedChapter(
        headers: Map<String, String>,
    ): ImportedChapter {
        return ImportedChapter(
            headers = headers,
            uciLines = listOf(listOf("e2e4", "e7e5")),
        )
    }
}
