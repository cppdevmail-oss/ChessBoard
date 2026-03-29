---
description: Debug a chess board rendering or move-input problem by reading board and controller state
---

Debug a chess board or move-input issue in the ChessBoard app.

The user's issue: $ARGUMENTS

Steps:
1. Read `ui/ChessBoard.kt` fully — note the gesture handling (tap vs drag), draw pass order, and how `setStartSquare` / `setDestinationSquareAndTryMove` are called
2. Read `boardmodel/GameController/d.kt` — note `tryMove`, `boardState`, `currentMoveIndex`, and training-phase move blocking
3. Read `boardmodel/BoardModel.kt` and `boardmodel/ChesslibMapper.kt` — note how FEN maps to rendered pieces
4. Based on the issue description, identify:
   - Whether the bug is in gesture detection, square coordinate math, `GameController` state, or FEN mapping
   - The exact lines involved
5. Propose a minimal fix

Rules:
- Don't change the Canvas draw block — mutations must stay in gesture callbacks
- Don't break the two-tap fallback when diagnosing drag issues
- `boardState` increments must happen exactly once per logical move to avoid double recomposition
