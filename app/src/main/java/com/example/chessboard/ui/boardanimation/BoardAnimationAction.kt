package com.example.chessboard.ui.boardanimation

/**
 * Render-queue commands for animated chess-board playback.
 * Keep only UI-scene transition commands here so the animation layer stays independent from chess-rule logic.
 * Do not add Compose runtime state, screen navigation, or LineController mutations to this file.
 * Validation date: 2026-07-10
 */

import com.example.chessboard.boardmodel.LastMoveHighlight
import com.example.chessboard.ui.boardrender.BoardRenderScene

sealed interface BoardAnimationAction

sealed interface AnimatedBoardMoveAction : BoardAnimationAction {
    val from: String
    val to: String
    val lastMoveHighlight: LastMoveHighlight
    val logicalPlyAfter: Int
    val durationMs: Int
}

data class ResetBoardSceneAction(
    val scene: BoardRenderScene,
    val renderPly: Int,
) : BoardAnimationAction

data class AnimateSimpleMoveAction(
    override val from: String,
    override val to: String,
    override val lastMoveHighlight: LastMoveHighlight,
    override val logicalPlyAfter: Int,
    override val durationMs: Int,
) : AnimatedBoardMoveAction

data class AnimateCaptureMoveAction(
    override val from: String,
    override val to: String,
    val capturedSquare: String,
    override val lastMoveHighlight: LastMoveHighlight,
    override val logicalPlyAfter: Int,
    override val durationMs: Int,
) : AnimatedBoardMoveAction
