package com.example.chessboard.testing

/**
 * Test-only helpers for FEN assertions.
 *
 * Keep in this file:
 * - normalization used by instrumented tests
 * - matchers that compare board state descriptions in a test-friendly way
 *
 * Do not add here:
 * - production app logic
 * - persistence or parsing rules used by runtime code
 */
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

fun normalizeFenForAssertion(fen: String): String {
    val fenParts = fen.trim().split(Regex("\\s+"))
    if (fenParts.size < 4) {
        return fen.trim()
    }

    return fenParts.mapIndexed { index, part ->
        if (index != 3) {
            return@mapIndexed part
        }

        "-"
    }.joinToString(separator = " ")
}

fun fenStateDescriptionMatcher(expectedFen: String): SemanticsMatcher {
    return SemanticsMatcher("StateDescription normalized fen equals $expectedFen") { semanticsNode ->
        val actualFen = semanticsNode.config.getOrNull(SemanticsProperties.StateDescription)
            ?: return@SemanticsMatcher false

        normalizeFenForAssertion(actualFen) == normalizeFenForAssertion(expectedFen)
    }
}
