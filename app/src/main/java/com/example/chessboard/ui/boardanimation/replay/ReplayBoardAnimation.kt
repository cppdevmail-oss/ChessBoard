package com.example.chessboard.ui.boardanimation.replay

/**
 * Shared replay-board animation helpers for non-interactive board flows.
 * Keep only reset/build helpers that convert LineController state and replay UCI moves into queued board-animation actions.
 * Do not add screen-specific selection logic, interactive board input handling, or unrelated UI orchestration here.
 * Validation date: 2026-07-10
 */

import com.example.chessboard.boardmodel.LastMoveHighlight
import com.example.chessboard.boardmodel.LineController
import com.example.chessboard.ui.boardanimation.AnimateCaptureMoveAction
import com.example.chessboard.ui.boardanimation.AnimateSimpleMoveAction
import com.example.chessboard.ui.boardanimation.AnimatedBoardMoveAction
import com.example.chessboard.ui.boardanimation.BoardAnimationQueueController
import com.example.chessboard.ui.boardanimation.ResetBoardSceneAction
import com.example.chessboard.ui.boardrender.BoardRenderPiece
import com.example.chessboard.ui.boardrender.BoardRenderScene
import com.example.chessboard.ui.boardrender.buildBoardRenderScene

internal fun resetAnimatedReplayBoard(
    boardAnimationController: BoardAnimationQueueController,
    lineController: LineController,
) {
    boardAnimationController.submit(
        ResetBoardSceneAction(
            scene = buildBoardRenderScene(
                position = lineController.getBoardPosition(),
                orientation = lineController.getSide(),
                lastMoveHighlight = lineController.getLastMoveHighlight(),
            ),
            renderPly = lineController.currentMoveIndex,
        )
    )
}

internal fun buildReplayNextMoveAnimationAction(
    uciMoves: List<String>,
    lineController: LineController,
    durationMs: Int,
): AnimatedBoardMoveAction? {
    val currentPly = lineController.currentMoveIndex
    val nextMoveUci = uciMoves.getOrNull(currentPly) ?: return null
    val currentScene = buildBoardRenderScene(
        position = lineController.getBoardPosition(),
        orientation = lineController.getSide(),
        lastMoveHighlight = lineController.getLastMoveHighlight(),
    )

    return buildReplayForwardMoveActionOrNull(
        scene = currentScene,
        moveUci = nextMoveUci,
        logicalPlyAfter = currentPly + 1,
        durationMs = durationMs,
    )
}

internal fun buildReplayForwardMoveActionOrNull(
    scene: BoardRenderScene,
    moveUci: String,
    logicalPlyAfter: Int,
    durationMs: Int,
): AnimatedBoardMoveAction? {
    if (moveUci.length != 4) {
        return null
    }

    val from = moveUci.substring(0, 2)
    val to = moveUci.substring(2, 4)
    val movingPiece = scene.pieces.find { piece -> piece.square == from } ?: return null
    val capturedPiece = scene.pieces.find { piece -> piece.square == to }
    if (isReplayCastlingMove(movingPiece, from, to)) {
        return null
    }
    if (isReplayEnPassantLikeMove(movingPiece, from, to, targetOccupied = capturedPiece != null)) {
        return null
    }
    if (capturedPiece != null) {
        return AnimateCaptureMoveAction(
            from = from,
            to = to,
            capturedSquare = to,
            lastMoveHighlight = LastMoveHighlight(from = from, to = to),
            logicalPlyAfter = logicalPlyAfter,
            durationMs = durationMs,
        )
    }

    return AnimateSimpleMoveAction(
        from = from,
        to = to,
        lastMoveHighlight = LastMoveHighlight(from = from, to = to),
        logicalPlyAfter = logicalPlyAfter,
        durationMs = durationMs,
    )
}

private fun isReplayCastlingMove(
    movingPiece: BoardRenderPiece,
    from: String,
    to: String,
): Boolean {
    val lowerLetter = movingPiece.letter.lowercaseChar()
    if (lowerLetter != 'k') {
        return false
    }

    return kotlin.math.abs(resolveReplaySquareFileIndex(from) - resolveReplaySquareFileIndex(to)) == 2
}

private fun isReplayEnPassantLikeMove(
    movingPiece: BoardRenderPiece,
    from: String,
    to: String,
    targetOccupied: Boolean,
): Boolean {
    val lowerLetter = movingPiece.letter.lowercaseChar()
    if (lowerLetter != 'p') {
        return false
    }
    if (targetOccupied) {
        return false
    }

    return resolveReplaySquareFileIndex(from) != resolveReplaySquareFileIndex(to)
}

private fun resolveReplaySquareFileIndex(square: String): Int {
    return square[0] - 'a'
}
