package com.example.chessboard.service

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.collections.ArrayDeque

/** Extracts PGN header tag values (e.g. "Event", "ECO") keyed by tag name. */
fun extractPgnHeaders(pgnText: String): Map<String, String> {
    val headerRegex = Regex("""\[(\w+)\s+"([^"]*)"\]""")
    return headerRegex.findAll(pgnText).associate { it.groupValues[1] to it.groupValues[2] }
}

/**
 * Parses a standard PGN string (SAN notation) into UCI move strings
 * (e.g. "e2e4", "g1f3", "e7e8q"). Stops at the first unrecognised token.
 */
fun parsePgnToUci(pgnText: String): List<String> {
    return parsePgnToUciLines(pgnText).firstOrNull().orEmpty()
}

/** Parses the PGN into all unique playable lines, including nested variations. */
fun parsePgnToUciLines(pgnText: String): List<List<String>> {
    val sanLines = extractSanLines(pgnText)

    return sanLines
        .reversed() // extractSanLines adds the main line last; reverse so it comes first
        .mapNotNull(::parseSanLineToUci)
        .distinctBy { it.joinToString(" ") }
}

/** Converts UCI strings into chesslib moves for persistence. */
fun uciMovesToMoves(uciMoves: List<String>): List<Move> {
    val board = Board()

    return uciMoves.map { uci ->
        val move = uciToMove(uci, board)
        board.doMove(move)
        move
    }
}

/** Builds the stored PGN format used by this app from a list of UCI strings. */
fun buildStoredPgnFromUci(
    uciMoves: List<String>,
    event: String,
    whiteName: String = "White",
    blackName: String = "Black"
): String {
    val sb = StringBuilder()

    sb.append("[Event \"$event\"]\n")
    sb.append("[White \"$whiteName\"]\n")
    sb.append("[Black \"$blackName\"]\n")
    sb.append("[Result \"*\"]\n\n")

    uciMoves.forEachIndexed { index, move ->
        if (index % 2 == 0) {
            sb.append("${index / 2 + 1}. ")
        }
        sb.append("$move ")
    }

    sb.append("*")
    return sb.toString().trim()
}

private fun extractSanLines(pgnText: String): List<List<String>> {
    val withoutComments = pgnText
        .replace(Regex("\\{[^}]*\\}"), " ")
        .replace(Regex(";[^\\n]*"), " ")

    val movesText = withoutComments.lines()
        .filterNot { it.trim().startsWith("[") }
        .joinToString(" ")

    val tokens = Regex("""\(|\)|\d+\.(?:\.\.)?|1-0|0-1|1/2-1/2|\*|\$\d+|[^\s()]+""")
        .findAll(movesText)
        .map { it.value.trim() }
        .filter { it.isNotBlank() }
        .toList()

    val lines = mutableListOf<List<String>>()
    val branchStack = ArrayDeque<List<String>>()
    var currentLine = mutableListOf<String>()

    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        when {
            token == "(" -> {
                branchStack.addLast(currentLine.toList())
                // Use the move number right after "(" to find the exact backtrack point.
                // e.g. "(4. Be2" means white's move 4 → ply = (4-1)*2 = 6 half-moves.
                val targetPly = variationStartPly(tokens.getOrNull(i + 1))
                    ?.coerceIn(0, currentLine.size)
                    ?: (currentLine.size - 1)
                currentLine = currentLine.take(targetPly).toMutableList()
            }
            token == ")" -> {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toList())
                currentLine = branchStack.removeLastOrNull()?.toMutableList() ?: mutableListOf()
            }
            token.startsWith("$") || token.matches(Regex("""\d+\.?(?:\.\.)?""")) || isResultToken(token) -> {
                // skip move numbers, NAG annotations and result tokens
            }
            else -> currentLine.add(token)
        }
        i++
    }

    if (currentLine.isNotEmpty()) lines.add(currentLine.toList())
    return lines
}

/** Returns the half-move index (ply) at which a variation starts given its first token. */
private fun variationStartPly(token: String?): Int? {
    if (token == null) return null
    return when {
        token.matches(Regex("""\d+\.""")) ->
            (token.dropLast(1).toIntOrNull() ?: return null).let { (it - 1) * 2 }
        token.matches(Regex("""\d+\.\.\.""")) ->
            (token.dropLast(3).toIntOrNull() ?: return null).let { (it - 1) * 2 + 1 }
        else -> null
    }
}

private fun parseSanLineToUci(tokens: List<String>): List<String>? {
    val board = Board()
    val uciMoves = mutableListOf<String>()

    for (token in tokens) {
        val uci = sanToUci(token, board) ?: return null
        val move = uciToMove(uci, board)

        if (!board.legalMoves().contains(move)) {
            return null
        }

        board.doMove(move)
        uciMoves.add(uci)
    }

    return uciMoves
}

private fun isResultToken(token: String): Boolean {
    return token == "*" || token == "1-0" || token == "0-1" || token == "1/2-1/2"
}

private fun uciToMove(uci: String, board: Board): Move {
    val fromSq = Square.fromValue(uci.take(2).uppercase())
    val toSq = Square.fromValue(uci.drop(2).take(2).uppercase())
    val promoChar = uci.getOrNull(4)
    val isWhite = board.sideToMove.name == "WHITE"
    val promotion = when (promoChar) {
        'q' -> if (isWhite) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
        'r' -> if (isWhite) Piece.WHITE_ROOK else Piece.BLACK_ROOK
        'b' -> if (isWhite) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
        'n' -> if (isWhite) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
        else -> Piece.NONE
    }

    return if (promotion != Piece.NONE) {
        Move(fromSq, toSq, promotion)
    } else {
        Move(fromSq, toSq)
    }
}

/**
 * Converts a single SAN token (e.g. "Nf3", "exd5", "O-O", "e8=Q") to a UCI string
 * given the current board state. Returns null if no legal move matches.
 */
private fun sanToUci(san: String, board: Board): String? {
    val cleaned = san.trimEnd('+', '#', '!', '?', ' ')
    if (cleaned.isBlank()) return null

    if (cleaned == "O-O-O" || cleaned == "0-0-0") {
        val isWhite = board.sideToMove.name == "WHITE"
        val move = board.legalMoves().find { m ->
            m.from.value().lowercase() == (if (isWhite) "e1" else "e8") &&
            m.to.value().lowercase() == (if (isWhite) "c1" else "c8")
        }
        return move?.let { "${it.from.value().lowercase()}${it.to.value().lowercase()}" }
    }
    if (cleaned == "O-O" || cleaned == "0-0") {
        val isWhite = board.sideToMove.name == "WHITE"
        val move = board.legalMoves().find { m ->
            m.from.value().lowercase() == (if (isWhite) "e1" else "e8") &&
            m.to.value().lowercase() == (if (isWhite) "g1" else "g8")
        }
        return move?.let { "${it.from.value().lowercase()}${it.to.value().lowercase()}" }
    }

    val promotionType: PieceType?
    val sanCore: String
    val eqIdx = cleaned.indexOf('=')
    if (eqIdx != -1) {
        sanCore = cleaned.substring(0, eqIdx)
        promotionType = charToPieceType(cleaned.getOrNull(eqIdx + 1))
    } else if (cleaned.length >= 3 && cleaned.last() in "QRBNqrbn" &&
        cleaned[cleaned.length - 2].isDigit() && cleaned[cleaned.length - 3].isLetter()) {
        sanCore = cleaned.dropLast(1)
        promotionType = charToPieceType(cleaned.last())
    } else {
        sanCore = cleaned
        promotionType = null
    }

    val isCapture = sanCore.contains('x')
    val withoutCapture = sanCore.replace("x", "")
    if (withoutCapture.length < 2) return null

    val destSquare = withoutCapture.takeLast(2).lowercase()
    val prefix = withoutCapture.dropLast(2)
    val legalMoves = board.legalMoves()
    val isWhite = board.sideToMove.name == "WHITE"

    return if (prefix.isNotEmpty() && prefix[0].isUpperCase()) {
        val pieceType = when (prefix[0]) {
            'N' -> PieceType.KNIGHT
            'B' -> PieceType.BISHOP
            'R' -> PieceType.ROOK
            'Q' -> PieceType.QUEEN
            'K' -> PieceType.KING
            else -> return null
        }
        val disambiguation = prefix.drop(1)
        val candidates = legalMoves.filter { m ->
            board.getPiece(m.from).pieceType == pieceType &&
            m.to.value().lowercase() == destSquare &&
            m.promotion == Piece.NONE
        }
        val matched = when {
            candidates.size == 1 -> candidates[0]
            disambiguation.isEmpty() -> candidates.firstOrNull()
            disambiguation.length == 1 && disambiguation[0].isDigit() ->
                candidates.find { it.from.value()[1] == disambiguation[0] }
            disambiguation.length == 1 ->
                candidates.find { it.from.value()[0].lowercaseChar() == disambiguation[0] }
            disambiguation.length == 2 ->
                candidates.find { it.from.value().lowercase() == disambiguation }
            else -> null
        }
        matched?.let { "${it.from.value().lowercase()}${it.to.value().lowercase()}" }
    } else {
        val pawnPiece = if (isWhite) Piece.WHITE_PAWN else Piece.BLACK_PAWN
        val effectivePromotionType = promotionType ?: run {
            val anyCandidatePromotes = legalMoves.any { m ->
                board.getPiece(m.from) == pawnPiece &&
                m.to.value().lowercase() == destSquare &&
                m.promotion != Piece.NONE
            }
            if (anyCandidatePromotes) PieceType.QUEEN else null
        }
        val promotionPiece = when (effectivePromotionType) {
            PieceType.QUEEN  -> if (isWhite) Piece.WHITE_QUEEN  else Piece.BLACK_QUEEN
            PieceType.ROOK   -> if (isWhite) Piece.WHITE_ROOK   else Piece.BLACK_ROOK
            PieceType.BISHOP -> if (isWhite) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
            PieceType.KNIGHT -> if (isWhite) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
            else -> Piece.NONE
        }
        val candidates = legalMoves.filter { m ->
            board.getPiece(m.from) == pawnPiece &&
            m.to.value().lowercase() == destSquare &&
            (promotionPiece == Piece.NONE && m.promotion == Piece.NONE ||
             promotionPiece != Piece.NONE && m.promotion == promotionPiece)
        }
        val matched = if (isCapture && prefix.isNotEmpty()) {
            candidates.find { it.from.value()[0].lowercaseChar() == prefix[0] }
        } else {
            candidates.firstOrNull()
        }
        matched?.let {
            buildString {
                append(it.from.value().lowercase())
                append(it.to.value().lowercase())
                if (it.promotion != Piece.NONE) {
                    append(it.promotion.pieceType.name.first().lowercaseChar())
                }
            }
        }
    }
}

private fun charToPieceType(c: Char?): PieceType? = when (c?.uppercaseChar()) {
    'Q' -> PieceType.QUEEN
    'R' -> PieceType.ROOK
    'B' -> PieceType.BISHOP
    'N' -> PieceType.KNIGHT
    else -> null
}
