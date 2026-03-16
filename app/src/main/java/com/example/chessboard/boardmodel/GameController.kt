package com.example.chessboard.boardmodel

import com.example.chessboard.ui.BoardOrientation
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move


class GameController (val inOrientation : BoardOrientation = BoardOrientation.WHITE) {
    private val board = Board()
    private var orientation = inOrientation

    private var startSquare : String? = null

    fun tryMove(from: String, to: String): Boolean {
        return try {
            println("try move from ${from} to ${to}")
            val move = Move(
                Square.fromValue(from.uppercase()),
                Square.fromValue(to.uppercase())
            )

            if (board.legalMoves().contains(move)) {
                println("Move ${move} is fucking legal")
                this.board.doMove(move)
                return true
            }
            false
        } catch (e: Exception) {
            println("Error on try move. Err ${e}")
            return false
        }
    }

    // const function
    fun getOrientation() : BoardOrientation {
        return this.orientation
    }

    // const function
    fun getFen(): String = this.board.fen

    // const function
    fun getBoardPosition() : BoardPosition {
        return ChesslibMapper.fromFen(getFen())
    }

    // const function
    fun getStartSquare() : String? {
        return startSquare
    }

    // const function
    fun canSelectSquare(square: String?): Boolean {
        val sqStr = square ?: return false
        val sq = Square.fromValue(sqStr.uppercase())

        val piece = getPieceWithLegalMovesFromSquare(square)
        if (piece == null) { return false }

        val moves = board.legalMoves()
        return moves.any { it.from == sq }
    }

    fun setStartSquare(square: String?) : Boolean {
        if (canSelectSquare(square)) {
            startSquare = square
            return true
        }
        startSquare = null
        return false
    }

    fun setDestinationSquareAndTryMove(destinationSquare: String?) : Boolean {
        if (destinationSquare == null) {
            return false
        }
        val piece = getPieceWithLegalMovesFromSquare(startSquare)
        if (piece == null) { return false }
        val wasMove = tryMove(startSquare!!, destinationSquare)

        startSquare = null

        return wasMove
    }

    private fun getPieceWithLegalMovesFromSquare(square: String?) : Piece? {
        if (square == null) { return null }

        val sq = Square.fromValue(square.uppercase())
        val piece = board.getPiece(sq)
        if (piece == Piece.NONE) {
            return null
        }
        if (piece.pieceSide != board.sideToMove) {
            return null
        }

        return piece
    }
}