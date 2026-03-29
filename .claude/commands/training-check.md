---
description: Audit the training flow for broken references, weight logic, and session state correctness
---

Audit the training system in this ChessBoard app for correctness issues.

Read and analyze the following files in order:
1. `service/TrainingService.kt`
2. `service/TrainSingleGameService.kt`
3. `ui/screen/trainSingleGame/TrainSingleGameLogic.kt`
4. `ui/screen/trainSingleGame/TrainSingleGameModels.kt`
5. `ui/screen/trainSingleGame/TrainSingleGameScreen.kt`
6. `repository/DatabaseProvider.kt` (training-related methods only)

Check for:
- **Broken game references**: Does `validateTraining` get called before a session starts? Are missing games handled gracefully?
- **Weight decrement logic**: Is weight decremented exactly once per completed line? Can it go negative or skip zero?
- **Session state transitions**: Do all `TrainingPhase` / session state enums have valid transitions? Are there states that can deadlock?
- **Board state sync**: Is `GameController.loadFromUciMoves` called with the correct `targetPly` at session start and after undo?
- **Move blocking**: Are manual board moves correctly blocked outside the `WAITING_FOR_MOVE` phase?
- **Coroutine scoping**: Are DB calls on `Dispatchers.IO`? Are state mutations on `Dispatchers.Main`?

Output a concise list of issues found (or "No issues found" if clean), with file:line references for each problem.
