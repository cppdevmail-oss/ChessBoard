package com.example.chessboard.ui.screen.trainSingleGame

/**
 * File role: groups the screen-level orchestration for a single-game training session.
 * Allowed here:
 * - Compose state, lifecycle effects, runtime-session restoration, and training callbacks
 * - wiring between pure training logic and rendered training UI
 * Not allowed here:
 * - reusable generic UI components that belong outside this screen package
 * - persistence helpers or database-facing logic unrelated to this screen flow
 * Validation date: 2026-04-25
 */

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.boardmodel.GameDraft
import com.example.chessboard.boardmodel.buildGameDraftFromSourceGame
import com.example.chessboard.runtimecontext.TrainingRuntimeContext
import com.example.chessboard.service.buildMoveLabels
import com.example.chessboard.ui.BoardOrientation
import com.example.chessboard.ui.components.AppBottomNavigation
import com.example.chessboard.ui.components.AppScreenScaffold
import com.example.chessboard.ui.components.AppTopBar
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.components.defaultAppBottomNavigationItems
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.resolvePlayerTier
import com.example.chessboard.ui.theme.AppDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TrainSingleGameScreenContainer(
    gameId: Long,
    trainingId: Long,
    trainingGameData: TrainSingleGameData,
    trainingRuntimeContext: TrainingRuntimeContext,
    keepLineIfZero: Boolean = false,
    hasNextTrainingGame: Boolean = false,
    sessionCurrent: Int = 0,
    sessionTotal: Int = 0,
    onTrainingFinished: (TrainSingleGameResult) -> Unit = {},
    onNextTrainingClick: (TrainSingleGameResult) -> Unit = {},
    autoNextLine: Boolean = false,
    onInterruptTrainingClick: () -> Unit,
    onOpenGameEditorClick: () -> Unit,
    onCloneGameClick: (GameDraft) -> Unit,
    onSearchByPositionClick: (String) -> Unit,
    onAnalyzeGameClick: (List<String>, Int) -> Unit,
    simpleViewEnabled: Boolean = false,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
) {
    val inDbProvider = screenContext.inDbProvider
    val scope = rememberCoroutineScope()
    var showLevelUp by remember { mutableStateOf(false) }
    var levelUpTierSymbol by remember { mutableStateOf("") }
    var levelUpLevel by remember { mutableStateOf(0) }
    var levelUpTitle by remember { mutableStateOf("") }
    var autoNextLine by remember { mutableStateOf(autoNextLine) }

    suspend fun checkAndRecordStats(result: TrainSingleGameResult): Boolean {
        val newLevel = withContext(Dispatchers.IO) {
            inDbProvider.recordTrainingGameStatsCheckingLevelUp(
                gameId = result.gameId,
                mistakesCount = result.mistakesCount,
            )
        }
        if (newLevel != null) {
            val tier = resolvePlayerTier(newLevel)
            val title = tier.titles.random()
            withContext(Dispatchers.IO) {
                inDbProvider.createUserProfileService().updateRankTitle(tier.name, title)
            }
            levelUpTierSymbol = tier.symbol
            levelUpLevel = newLevel
            levelUpTitle = title
            showLevelUp = true
            return true
        }
        return false
    }

    TrainSingleGameScreen(
        gameId = gameId,
        trainingId = trainingId,
        trainingGameData = trainingGameData,
        trainingRuntimeContext = trainingRuntimeContext,
        hasNextTrainingGame = hasNextTrainingGame,
        sessionCurrent = sessionCurrent,
        sessionTotal = sessionTotal,
        showLevelUp = showLevelUp,
        levelUpTierSymbol = levelUpTierSymbol,
        levelUpLevel = levelUpLevel,
        levelUpTitle = levelUpTitle,
        onLevelUpDismiss = { showLevelUp = false },
        onLineCompleted = { result ->
            scope.launch { checkAndRecordStats(result) }
        },
        checkAndRecordStats = { result -> checkAndRecordStats(result) },
        onTrainingFinished = { result ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    inDbProvider.finishTrainingGame(
                        trainingId = result.trainingId,
                        gameId = result.gameId,
                        mistakesCount = result.mistakesCount,
                        keepLineIfZero = keepLineIfZero
                    )
                }
                onTrainingFinished(result)
            }
        },
        onNextTrainingClick = { result ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    inDbProvider.finishTrainingGame(
                        trainingId = result.trainingId,
                        gameId = result.gameId,
                        mistakesCount = result.mistakesCount,
                        keepLineIfZero = keepLineIfZero
                    )
                }
                onNextTrainingClick(result)
            }
        },
        autoNextLine = autoNextLine,
        onAutoNextLineChange = { enabled ->
            autoNextLine = enabled
            scope.launch(Dispatchers.IO) {
                inDbProvider.createUserProfileService().updateAutoNextLine(enabled)
            }
        },
        onInterruptTrainingClick = onInterruptTrainingClick,
        onBackClick = screenContext.onBackClick,
        onNavigate = screenContext.onNavigate,
        onOpenGameEditorClick = onOpenGameEditorClick,
        onCloneGameClick = onCloneGameClick,
        onSearchByPositionClick = onSearchByPositionClick,
        onAnalyzeGameClick = onAnalyzeGameClick,
        simpleViewEnabled = simpleViewEnabled,
        modifier = modifier
    )
}

@Composable
private fun TrainSingleGameScreen(
    gameId: Long,
    trainingId: Long,
    trainingGameData: TrainSingleGameData,
    trainingRuntimeContext: TrainingRuntimeContext,
    hasNextTrainingGame: Boolean = false,
    sessionCurrent: Int = 0,
    sessionTotal: Int = 0,
    showLevelUp: Boolean = false,
    levelUpTierSymbol: String = "",
    levelUpLevel: Int = 0,
    levelUpTitle: String = "",
    onLevelUpDismiss: () -> Unit = {},
    onLineCompleted: (TrainSingleGameResult) -> Unit = {},
    checkAndRecordStats: suspend (TrainSingleGameResult) -> Boolean = { false },
    onTrainingFinished: (TrainSingleGameResult) -> Unit = {},
    onNextTrainingClick: (TrainSingleGameResult) -> Unit = {},
    autoNextLine: Boolean = false,
    onAutoNextLineChange: (Boolean) -> Unit = {},
    onInterruptTrainingClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    onOpenGameEditorClick: () -> Unit,
    onCloneGameClick: (GameDraft) -> Unit,
    onSearchByPositionClick: (String) -> Unit,
    onAnalyzeGameClick: (List<String>, Int) -> Unit,
    simpleViewEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val loadedGame = trainingGameData.game
    var selectedNavItem by remember { mutableStateOf<ScreenType>(ScreenType.Home) }
    var showShowLineDialog by remember(loadedGame.id) { mutableStateOf(false) }
    val uciMoves = trainingGameData.uciMoves
    val startFen = trainingGameData.startFen
    val hasMoveCap = trainingGameData.hasMoveCap
    val lineFingerprint = remember(startFen, uciMoves) {
        resolveTrainingLineFingerprint(
            startFen = startFen,
            uciMoves = uciMoves,
        )
    }
    val moveLabels = remember(uciMoves) { buildMoveLabels(uciMoves) }
    var uiState by remember(loadedGame.id) { mutableStateOf(TrainSingleGameUiState()) }
    var hasInitializedSession by remember(loadedGame.id) { mutableStateOf(false) }
    var showLineJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val trainingSides = remember(loadedGame.sideMask) {
        resolveTrainingOrientations(loadedGame.sideMask)
    }
    val currentOrientation = trainingSides.getOrNull(uiState.currentSideIndex) ?: BoardOrientation.WHITE
    val gameController = remember(currentOrientation) { GameController(currentOrientation) }

    fun resetToTrainingStart() {
        if (startFen != null) gameController.loadFromFen(startFen)
        else gameController.resetToStartPosition()
    }

    fun startTrainingSession(baseState: TrainSingleGameUiState): TrainSingleGameUiState {
        return advanceProgramMoves(
            uiState = buildStartTrainingState(baseState),
            gameController = gameController,
            uciMoves = uciMoves,
            currentOrientation = currentOrientation,
            sidesCount = trainingSides.size,
            startFen = startFen,
            hasMoveCap = hasMoveCap,
        )
    }

    fun startShowLine() {
        Log.d(
            TrainSingleGameLogTag,
            "Show line clicked. gameId= trainingId= boardState= moves="
        )

        showLineJob?.cancel()
        showLineJob = null
        resetToTrainingStart()
        uiState = buildShowLineState(uiState)
        showLineJob = scope.launch {
            try {
                Log.d(
                    TrainSingleGameLogTag,
                    "Starting runShowLine coroutine. boardState= movesCount="
                )
                uiState = runShowLine(
                    uiState = uiState,
                    gameController = gameController,
                    uciMoves = uciMoves,
                    moveDelayMs = resolveShowLineMoveDelayMs(uiState.showLineMoveDelayInput),
                    startFen = startFen,
                )
            } finally {
                showLineJob = null
            }
        }
    }

    fun createNextTrainingClickAction(): (() -> Unit)? {
        if (!hasNextTrainingGame) {
            return null
        }

        val completionDialog = uiState.completionDialog ?: return null
        if (completionDialog.hasNextSide) {
            return null
        }

        return {
            val result = TrainSingleGameResult(
                gameId = gameId,
                trainingId = trainingId,
                mistakesCount = uiState.mistakesCount,
            )
            hasInitializedSession = false
            trainingRuntimeContext.clearGameProgress(trainingId, gameId)
            uiState = uiState.copy(completionDialog = null)
            onNextTrainingClick(result)
        }
    }

    LaunchedEffect(gameController, loadedGame.id) {
        showLineJob?.cancel()
        showLineJob = null
        val savedProgress = trainingRuntimeContext.restoreGameProgress(trainingId, loadedGame.id)
        if (
            savedProgress != null &&
            shouldRestoreTrainingSnapshot(
                snapshotFingerprint = savedProgress.lineFingerprint,
                currentFingerprint = lineFingerprint,
            )
        ) {
            gameController.loadFromUciMoves(
                uciMoves = uciMoves,
                targetPly = savedProgress.currentPly,
                startFen = startFen,
            )
            uiState = savedProgress.uiState
            hasInitializedSession = true
            return@LaunchedEffect
        }

        if (savedProgress != null) {
            trainingRuntimeContext.clearGameProgress(trainingId, loadedGame.id)
        }

        resetToTrainingStart()
        val resetState = resetSessionState(uiState)
        uiState = startTrainingSession(resetState)
        hasInitializedSession = true
    }

    SideEffect {
        gameController.setUserMovesEnabled(resolveBoardInteractionEnabled(uiState))
        gameController.setAllowedMoveUci(null)
    }

    LaunchedEffect(uiState.wrongMoveSquare) {
        if (uiState.wrongMoveSquare != null) {
            delay(1000L)
            uiState = uiState.copy(wrongMoveSquare = null)
        }
    }

    LaunchedEffect(uiState.hintSquare) {
        if (uiState.hintSquare != null) {
            delay(1500L)
            uiState = uiState.copy(hintSquare = null)
        }
    }

    LaunchedEffect(
        uiState.phase,
        gameController.boardState,
        uiState.expectedPly,
        currentOrientation,
        uciMoves
    ) {
        uiState = handleTrainingProgress(
            uiState = uiState,
            gameController = gameController,
            uciMoves = uciMoves,
            currentOrientation = currentOrientation,
            sidesCount = trainingSides.size,
            startFen = startFen,
            hasMoveCap = hasMoveCap,
        )
    }

    LaunchedEffect(uiState, gameController.currentMoveIndex, trainingId, loadedGame.id) {
        if (!hasInitializedSession) {
            return@LaunchedEffect
        }

        trainingRuntimeContext.saveGameProgress(
            trainingId = trainingId,
            gameId = loadedGame.id,
            currentPly = gameController.currentMoveIndex,
            lineFingerprint = lineFingerprint,
            uiState = uiState,
        )
    }

    // Fire stats as soon as the final completion dialog appears, before Finish is pressed.
    // Skipped when auto-next is on — auto-next calls checkAndRecordStats inline so it can
    // await the result and react to level-up before deciding whether to navigate.
    val completionDialog = uiState.completionDialog
    LaunchedEffect(completionDialog) {
        if (autoNextLine) return@LaunchedEffect
        if (completionDialog != null && !completionDialog.hasNextSide) {
            onLineCompleted(
                TrainSingleGameResult(
                    gameId = gameId,
                    trainingId = trainingId,
                    mistakesCount = uiState.mistakesCount,
                )
            )
        }
    }

    // Tracks whether stats were recorded for the current completion so the effect doesn't
    // double-record when it restarts after the level-up dialog is dismissed.
    // remember(completionDialog) resets this flag on each new completion.
    var statsRecordedForCompletion by remember(completionDialog) { mutableStateOf(false) }

    // Auto-advance to the next side or next game when auto-next is enabled.
    // showLevelUp is a key so the effect restarts whenever the level-up dialog appears or
    // is dismissed — no snapshotFlow or arbitrary delay needed.
    LaunchedEffect(completionDialog, autoNextLine, showLevelUp) {
        val dialog = completionDialog ?: return@LaunchedEffect
        if (!autoNextLine) return@LaunchedEffect
        if (showLevelUp) return@LaunchedEffect  // wait until user dismisses the level-up dialog
        if (!dialog.hasNextSide && !hasNextTrainingGame) return@LaunchedEffect

        if (dialog.hasNextSide) {
            uiState = handleCompletionFinish(
                uiState = uiState,
                gameId = gameId,
                trainingId = trainingId,
                onTrainingFinished = onTrainingFinished
            )
            return@LaunchedEffect
        }

        val result = TrainSingleGameResult(
            gameId = gameId,
            trainingId = trainingId,
            mistakesCount = uiState.mistakesCount,
        )
        if (!statsRecordedForCompletion) {
            statsRecordedForCompletion = true
            val leveledUp = checkAndRecordStats(result)
            if (leveledUp) return@LaunchedEffect  // showLevelUp → true triggers key restart → exits above
        }

        hasInitializedSession = false
        trainingRuntimeContext.clearGameProgress(trainingId, gameId)
        uiState = uiState.copy(completionDialog = null)
        onNextTrainingClick(result)
    }

    // Keeps the event handlers together so the main render block stays compact.
    fun createContentActions(): TrainSingleGameContentActions {
        return TrainSingleGameContentActions(
            onShowLineClick = {
                showShowLineDialog = true
            },
            onConfirmShowLineClick = {
                showShowLineDialog = false
                startShowLine()
            },
            onDismissShowLineDialogClick = {
                showShowLineDialog = false
            },
            onStopShowLineClick = {
                showLineJob?.cancel()
                showLineJob = null
                uiState = uiState.copy(phase = TrainSingleGamePhase.Idle)
            },
            onAnalyzeGameClick = {
                onAnalyzeGameClick(
                    trainingGameData.analysisUciMoves,
                    resolveTrainingAnalysisInitialPly(
                        trainingGameData = trainingGameData,
                        currentPly = gameController.currentMoveIndex,
                    ),
                )
            },
            onStartTrainingClick = {
                resetToTrainingStart()
                uiState = startTrainingSession(uiState)
            },
            onStopTrainingClick = {
                showLineJob?.cancel()
                showLineJob = null
                resetToTrainingStart()
                uiState = resetSessionState(uiState)
            },
            onHintClick = {
                val fromSquare = uciMoves.getOrNull(uiState.expectedPly)?.take(2)
                if (fromSquare != null && uiState.phase == TrainSingleGamePhase.Training) {
                    uiState = uiState.copy(
                        hintSquare = fromSquare,
                        mistakesCount = uiState.mistakesCount + 1,
                    )
                }
            },
            onMakeCorrectMoveClick = {
                uiState = handleCorrectMove(
                    uiState = uiState,
                    gameController = gameController,
                    uciMoves = uciMoves,
                    currentOrientation = currentOrientation,
                    sidesCount = trainingSides.size,
                    startFen = startFen,
                    hasMoveCap = hasMoveCap,
                )
            },
            onShowLineMoveDelayInputChange = { input ->
                uiState = uiState.copy(
                    showLineMoveDelayInput = formatShowLineMoveDelayInput(
                        input.toLongOrNull() ?: ShowLineMoveDelayMs
                    )
                )
            },
            onMovePlyClick = { ply ->
                if (uiState.showLineCompleted) {
                    gameController.loadFromUciMoves(uciMoves, ply, startFen)
                }
            },
            onPrevMoveClick = {
                if (uiState.showLineCompleted && gameController.canUndo) {
                    gameController.undoMove()
                }
            },
            onNextMoveClick = {
                if (uiState.showLineCompleted && gameController.canRedo) {
                    gameController.redoMove()
                }
            },
            onResetMovesClick = {
                if (uiState.showLineCompleted) {
                    gameController.loadFromUciMoves(uciMoves, 0, startFen)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
    AppScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                AppTopBar(
                    title = "Train Game",
                    onBackClick = onBackClick,
                    actions = {
                        if (!simpleViewEnabled) {
                            IconButton(
                                onClick = {
                                    onSearchByPositionClick(gameController.getFen())
                                }
                            ) {
                                IconMd(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search by position",
                                )
                            }
                            IconButton(
                                onClick = {
                                    onCloneGameClick(
                                        buildGameDraftFromSourceGame(loadedGame)
                                    )
                                }
                            ) {
                                IconMd(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Clone game",
                                )
                            }
                            IconButton(onClick = onOpenGameEditorClick) {
                                IconMd(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit game",
                                )
                            }
                        }
                        IconButton(onClick = onInterruptTrainingClick) {
                            IconMd(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Interrupt training",
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            AppBottomNavigation(
                items = defaultAppBottomNavigationItems(),
                selectedItem = selectedNavItem,
                onItemSelected = {
                    selectedNavItem = it
                    onNavigate(it)
                }
            )
        }
    ) { paddingValues ->
        val autoNextWillAdvance = autoNextLine && completionDialog != null &&
            (completionDialog.hasNextSide || hasNextTrainingGame)

        if (!showLevelUp) {
            RenderCompletionDialog(
                dialogState = if (autoNextWillAdvance) null else uiState.completionDialog,
                onRepeatClick = {
                    resetToTrainingStart()
                    uiState = buildRepeatVariationState(uiState)
                },
                onFinishClick = {
                    val isFinalCompletion = uiState.completionDialog?.hasNextSide != true
                    if (isFinalCompletion) {
                        hasInitializedSession = false
                        trainingRuntimeContext.clearGameProgress(trainingId, gameId)
                    }
                    uiState = handleCompletionFinish(
                        uiState = uiState,
                        gameId = gameId,
                        trainingId = trainingId,
                        onTrainingFinished = onTrainingFinished
                    )
                },
                onNextTrainingClick = createNextTrainingClickAction()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(AppDimens.spaceLg))
            TrainSingleGameContent(
                simpleViewEnabled = simpleViewEnabled,
                state = TrainSingleGameContentState(
                    gameId = gameId,
                    trainingId = trainingId,
                    trainingGameData = trainingGameData,
                    currentOrientation = currentOrientation,
                    sidesCount = trainingSides.size,
                    sessionCurrent = sessionCurrent,
                    sessionTotal = sessionTotal,
                    currentPly = gameController.currentMoveIndex,
                    moveLabels = moveLabels,
                    phase = uiState.phase,
                    mistakesCount = uiState.mistakesCount,
                    showLineMoveDelayInput = uiState.showLineMoveDelayInput,
                    showLineCompleted = uiState.showLineCompleted,
                    wrongMoveSquare = uiState.wrongMoveSquare,
                    hintSquare = uiState.hintSquare
                ),
                gameController = gameController,
                actions = createContentActions(),
                showShowLineDialog = showShowLineDialog,
            )
        }
    }

    if (showLevelUp) {
        LevelUpDialog(
            tierSymbol = levelUpTierSymbol,
            levelNumber = levelUpLevel,
            rankTitle = levelUpTitle,
            onDismiss = onLevelUpDismiss,
        )
    }
    } // Box
}

