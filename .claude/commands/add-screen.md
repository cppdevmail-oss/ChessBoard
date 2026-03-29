---
description: Scaffold a new screen following the Container+Stateless pattern used in this project
---

Scaffold a new screen for the ChessBoard app following the project's Container+Stateless pattern.

The user wants to add a screen named: $ARGUMENTS

Steps:
1. Add a new entry to `ScreenType` in `ui/screen/ScrenTypes.kt`
2. Create `ui/screen/<ScreenName>Screen.kt` with:
   - A `<ScreenName>ScreenContainer` composable that handles state, DB calls (via `DatabaseProvider`), and coroutine scopes
   - A pure stateless `<ScreenName>Screen` composable that receives only data and callbacks
   - DB operations on `Dispatchers.IO`, state updates on `Dispatchers.Main`
3. Wire the new screen into `MainActivity.kt` in the `when(currentScreen)` block
4. Update the File Map section in `CLAUDE.md` with the new screen entry

Rules:
- Never call DAOs directly from screens — always use `DatabaseProvider`
- No comments or docstrings on code you didn't change
- Don't add features beyond what the user asked for
- Use existing theme tokens from CLAUDE.md (Background, ButtonColor, TextColor, TrainingAccentTeal, etc.)
