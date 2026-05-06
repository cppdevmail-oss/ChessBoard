package com.example.chessboard.ui.screen.analysis

/**
 * Isolated game-analysis screen for exploring chess lines without saving them.
 *
 * Keep analysis screen state wiring, board controls, and analysis layout here. Do not add
 * app-level navigation registration, database persistence, or training-specific workflows here.
 */
import android.content.ClipData
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chessboard.R
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.boardmodel.GameVariationLineState
import com.example.chessboard.boardmodel.InitialBoardFen
import com.example.chessboard.service.buildAnalysisPgn
import com.example.chessboard.ui.GameAnalysisContentTestTag
import com.example.chessboard.ui.GameAnalysisMoveControlsTestTag
import com.example.chessboard.ui.GameAnalysisNextMoveTestTag
import com.example.chessboard.ui.GameAnalysisPreviousMoveTestTag
import com.example.chessboard.ui.GameAnalysisResetMovesTestTag
import com.example.chessboard.ui.GameAnalysisSearchActionTestTag
import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.AppLoadingDialog
import com.example.chessboard.ui.components.HomeIconButton
import com.example.chessboard.ui.components.AppMessageDialog
import com.example.chessboard.ui.components.AppScreenScaffold
import com.example.chessboard.ui.components.AppTopBar
import com.example.chessboard.ui.components.BoardActionNavigationBar
import com.example.chessboard.ui.components.BoardActionNavigationItem
import com.example.chessboard.ui.components.ChessBoardSection
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.components.ScreenSection
import com.example.chessboard.ui.screen.EditableGameSide
import com.example.chessboard.ui.screen.ScreenContainerContext
import com.example.chessboard.ui.screen.ScreenType
import com.example.chessboard.ui.screen.gameNotation.GameMoveTreeSection
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingIconInactive
import com.github.bhlangonijr.chesslib.Piece
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GameAnalysisInitialPosition {
    data object StartPosition : GameAnalysisInitialPosition

    data class FromFen(
        val fen: String,
    ) : GameAnalysisInitialPosition

    data class FromGameLine(
        val uciMoves: List<String>,
        val initialPly: Int = uciMoves.size,
    ) : GameAnalysisInitialPosition
}

@Composable
fun GameAnalysisScreenContainer(
    screenContext: ScreenContainerContext,
    initialPosition: GameAnalysisInitialPosition = GameAnalysisInitialPosition.StartPosition,
    onSearchByPositionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gameController = remember { GameController() }
    var variationState by remember { mutableStateOf(GameVariationLineState()) }
    var selectedSide by remember { mutableStateOf(EditableGameSide.AS_WHITE) }
    val startFen = resolveAnalysisStartFen(initialPosition)

    LaunchedEffect(initialPosition) {
        selectedSide = resolveInitialAnalysisSide(initialPosition)
        variationState = resolveInitialVariationState(initialPosition)
        loadInitialAnalysisPosition(
            gameController = gameController,
            initialPosition = initialPosition,
        )
    }

    LaunchedEffect(selectedSide) {
        gameController.setOrientation(selectedSide.orientation)
    }

    LaunchedEffect(gameController) {
        snapshotFlow { gameController.boardState }.collectLatest {
            variationState = resolveNextVariationState(
                variationState = variationState,
                gameController = gameController,
            )
        }
    }

    fun syncVariationState() {
        variationState = resolveNextVariationState(
            variationState = variationState,
            gameController = gameController,
        )
    }

    fun undoAnalysisMove() {
        if (!gameController.undoMove()) {
            return
        }

        syncVariationState()
    }

    fun redoAnalysisMove() {
        if (!gameController.redoMove()) {
            return
        }

        syncVariationState()
    }

    fun resetAnalysisPosition() {
        val backingLine = variationState.backingLineFor()
        if (backingLine.isEmpty()) {
            loadOriginAnalysisPosition(
                gameController = gameController,
                startFen = startFen,
            )
            variationState = variationState.selectPath(emptyList())
            syncVariationState()
            return
        }

        gameController.loadFromUciMoves(
            uciMoves = backingLine,
            targetPly = 0,
            startFen = startFen,
        )
        variationState = variationState.selectPath(emptyList())
        syncVariationState()
    }

    GameAnalysisScreen(
        gameController = gameController,
        variationLines = variationState.lines,
        startFen = startFen,
        selectedSide = selectedSide,
        onSideSelected = { selectedSide = it },
        onBackClick = screenContext.onBackClick,
        onHomeClick = { screenContext.onNavigate(ScreenType.Home) },
        onNavigate = screenContext.onNavigate,
        onSearchByPositionClick = { onSearchByPositionClick(gameController.getFen()) },
        onPreviousMoveClick = ::undoAnalysisMove,
        onNextMoveClick = ::redoAnalysisMove,
        onResetMovesClick = ::resetAnalysisPosition,
        modifier = modifier,
    )
}

@Composable
internal fun GameAnalysisScreen(
    gameController: GameController,
    variationLines: List<List<String>>,
    startFen: String?,
    selectedSide: EditableGameSide,
    onSideSelected: (EditableGameSide) -> Unit,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    onSearchByPositionClick: () -> Unit,
    onPreviousMoveClick: () -> Unit = {},
    onNextMoveClick: () -> Unit = {},
    onResetMovesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    @Suppress("UNUSED_VARIABLE")
    val boardState = gameController.boardState
    val canUndo = gameController.canUndo
    val canRedo = gameController.canRedo
    val moveTreeMaxHeight = LocalConfiguration.current.screenHeightDp.dp / 3
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val canCopyAnalysisPgn = variationLines.isNotEmpty()
    var isBuildingAnalysisPgn by remember { mutableStateOf(false) }
    var showPgnCopiedDialog by remember { mutableStateOf(false) }
    var showPgnUnavailableDialog by remember { mutableStateOf(false) }

    fun copyAnalysisPgn() {
        if (!canCopyAnalysisPgn || isBuildingAnalysisPgn) {
            return
        }

        coroutineScope.launch {
            isBuildingAnalysisPgn = true
            try {
                val analysisPgn = withContext(Dispatchers.Default) {
                    buildAnalysisPgn(variationLines)
                }

                if (analysisPgn.isBlank()) {
                    showPgnUnavailableDialog = true
                    return@launch
                }

                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData.newPlainText(
                            "Analysis PGN",
                            analysisPgn,
                        )
                    )
                )
                showPgnCopiedDialog = true
            } finally {
                isBuildingAnalysisPgn = false
            }
        }
    }

    AppScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = "Analyze Game",
                subtitle = "Explore lines without saving",
                onBackClick = onBackClick,
                actions = {
                    HomeIconButton(onClick = onHomeClick)
                    IconButton(
                        onClick = ::copyAnalysisPgn,
                        enabled = canCopyAnalysisPgn,
                    ) {
                        IconMd(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy analysis PGN",
                            tint = if (canCopyAnalysisPgn) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                        )
                    }
                    IconButton(
                        onClick = onSearchByPositionClick,
                        modifier = Modifier.testTag(GameAnalysisSearchActionTestTag),
                    ) {
                        IconMd(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search by position",
                        )
                    }
                },
            )
        },
        bottomBar = {
            GameAnalysisBoardControlsBar(
                selectedSide = selectedSide,
                onSideSelected = onSideSelected,
                canUndo = canUndo,
                canRedo = canRedo,
                onPreviousMoveClick = onPreviousMoveClick,
                onNextMoveClick = onNextMoveClick,
                onResetMovesClick = onResetMovesClick,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag(GameAnalysisContentTestTag),
            contentPadding = PaddingValues(
                horizontal = AppDimens.spaceLg,
                vertical = AppDimens.spaceLg,
            ),
        ) {
            item {
                ScreenSection {
                    ChessBoardSection(gameController = gameController)
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.spaceLg))
                ScreenSection {
                    GameMoveTreeSection(
                        importedUciLines = variationLines,
                        gameController = gameController,
                        startFen = startFen,
                        maxContentHeight = moveTreeMaxHeight,
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.spaceLg))
            }
        }
    }

    if (showPgnCopiedDialog) {
        AppMessageDialog(
            title = "PGN copied",
            message = "Analysis PGN was copied to the clipboard.",
            onDismiss = { showPgnCopiedDialog = false },
        )
    }

    if (showPgnUnavailableDialog) {
        AppMessageDialog(
            title = "PGN unavailable",
            message = "Analysis PGN could not be built.",
            onDismiss = { showPgnUnavailableDialog = false },
        )
    }

    if (isBuildingAnalysisPgn) {
        AppLoadingDialog(
            title = "Building PGN",
            message = "Preparing analysis PGN...",
        )
    }

}

@Composable
private fun GameAnalysisBoardControlsBar(
    selectedSide: EditableGameSide,
    onSideSelected: (EditableGameSide) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onPreviousMoveClick: () -> Unit,
    onNextMoveClick: () -> Unit,
    onResetMovesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoardActionNavigationBar(
        modifier = modifier.testTag(GameAnalysisMoveControlsTestTag),
        items = EditableGameSide.entries.map { side ->
            BoardActionNavigationItem(
                label = if (side == EditableGameSide.AS_WHITE) "White" else "Black",
                selected = side == selectedSide,
                onClick = { onSideSelected(side) },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_king),
                    contentDescription = side.toDisplayText(),
                    tint = if (side == selectedSide) TrainingAccentTeal else TrainingIconInactive,
                    modifier = Modifier.size(AppIconSizes.Lg),
                )
            }
        } + listOf(
            BoardActionNavigationItem(
                label = "Reset",
                enabled = canUndo,
                modifier = Modifier.testTag(GameAnalysisResetMovesTestTag),
                onClick = onResetMovesClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = if (canUndo) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                )
            },
            BoardActionNavigationItem(
                label = "Back",
                enabled = canUndo,
                modifier = Modifier.testTag(GameAnalysisPreviousMoveTestTag),
                onClick = onPreviousMoveClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous move",
                    tint = if (canUndo) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                )
            },
            BoardActionNavigationItem(
                label = "Forward",
                enabled = canRedo,
                modifier = Modifier.testTag(GameAnalysisNextMoveTestTag),
                onClick = onNextMoveClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next move",
                    tint = if (canRedo) TrainingIconInactive else TrainingIconInactive.copy(alpha = 0.5f),
                )
            },
        ),
    )
}

private fun resolveInitialAnalysisSide(
    initialPosition: GameAnalysisInitialPosition,
): EditableGameSide {
    if (initialPosition == GameAnalysisInitialPosition.StartPosition) {
        return EditableGameSide.AS_WHITE
    }

    if (initialPosition is GameAnalysisInitialPosition.FromFen) {
        if (resolveAnalysisFenSideToken(initialPosition.fen) == "b") {
            return EditableGameSide.AS_BLACK
        }

        return EditableGameSide.AS_WHITE
    }

    val gameLinePosition = initialPosition as GameAnalysisInitialPosition.FromGameLine
    if (gameLinePosition.initialPly.coerceAtLeast(0) % 2 == 1) {
        return EditableGameSide.AS_BLACK
    }

    return EditableGameSide.AS_WHITE
}

private fun resolveAnalysisStartFen(initialPosition: GameAnalysisInitialPosition): String? {
    if (initialPosition is GameAnalysisInitialPosition.FromFen) {
        return normalizeAnalysisFen(initialPosition.fen)
    }

    return null
}

private fun resolveInitialVariationState(
    initialPosition: GameAnalysisInitialPosition,
): GameVariationLineState {
    if (initialPosition !is GameAnalysisInitialPosition.FromGameLine) {
        return GameVariationLineState()
    }

    val normalizedMoves = normalizeUciPath(initialPosition.uciMoves)
    val selectedPath = normalizedMoves.take(initialPosition.initialPly.coerceAtLeast(0))
    return GameVariationLineState()
        .recordPlayedPath(normalizedMoves)
        .selectPath(selectedPath)
}

private fun loadInitialAnalysisPosition(
    gameController: GameController,
    initialPosition: GameAnalysisInitialPosition,
) {
    when (initialPosition) {
        GameAnalysisInitialPosition.StartPosition -> {
            gameController.resetToStartPosition()
        }
        is GameAnalysisInitialPosition.FromFen -> {
            loadOriginAnalysisPosition(
                gameController = gameController,
                startFen = normalizeAnalysisFen(initialPosition.fen),
            )
        }
        is GameAnalysisInitialPosition.FromGameLine -> {
            gameController.loadFromUciMoves(
                uciMoves = normalizeUciPath(initialPosition.uciMoves),
                targetPly = initialPosition.initialPly,
            )
        }
    }
}

private fun loadOriginAnalysisPosition(
    gameController: GameController,
    startFen: String?,
) {
    val normalizedFen = normalizeAnalysisFen(startFen)
    if (normalizedFen.isBlank()) {
        gameController.resetToStartPosition()
        return
    }

    if (gameController.loadFromFen(normalizedFen)) {
        return
    }

    gameController.loadFromFen(InitialBoardFen)
}

internal fun resolveNextVariationState(
    variationState: GameVariationLineState,
    gameController: GameController,
): GameVariationLineState {
    val fullLine = resolveControllerUciLine(gameController)
    val currentPath = resolveControllerUciLine(
        gameController = gameController,
        upToPly = gameController.currentMoveIndex,
    )

    if (currentPath.isEmpty()) {
        if (fullLine.isNotEmpty()) {
            return variationState
                .recordPlayedPath(fullLine)
                .selectPath(emptyList())
        }

        return variationState.selectPath(emptyList())
    }

    if (currentPath.size < fullLine.size) {
        return variationState.selectPath(currentPath)
    }

    return variationState.recordPlayedPath(currentPath)
}

private fun resolveControllerUciLine(
    gameController: GameController,
    upToPly: Int = gameController.getMovesCopy().size,
): List<String> {
    return gameController.getMovesCopy().take(upToPly).map { move ->
        buildString {
            append(move.from.value().lowercase())
            append(move.to.value().lowercase())
            if (move.promotion != Piece.NONE) {
                append(move.promotion.pieceType.name.first().lowercaseChar())
            }
        }
    }
}

private fun normalizeUciPath(moves: List<String>): List<String> {
    return moves.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}

private fun resolveAnalysisFenSideToken(fen: String): String {
    return fen.trim().split(Regex("\\s+")).getOrNull(1) ?: "w"
}

private fun normalizeAnalysisFen(fen: String?): String {
    val normalizedFen = fen?.trim().orEmpty()
    if (normalizedFen.isBlank()) {
        return ""
    }

    val fenParts = normalizedFen.split(Regex("\\s+"))

    if (fenParts.size >= 6) {
        return normalizedFen
    }

    if (fenParts.size == 5) {
        return "$normalizedFen 1"
    }

    if (fenParts.size == 4) {
        return "$normalizedFen 0 1"
    }

    if (fenParts.size == 3) {
        return "$normalizedFen - 0 1"
    }

    if (fenParts.size == 2) {
        return "$normalizedFen - - 0 1"
    }

    return "$normalizedFen w - - 0 1"
}
