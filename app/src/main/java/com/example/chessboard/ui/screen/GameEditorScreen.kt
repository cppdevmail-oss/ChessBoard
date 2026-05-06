package com.example.chessboard.ui.screen

import com.example.chessboard.ui.components.AppIconSizes
import com.example.chessboard.ui.components.DeleteIconButton
import com.example.chessboard.ui.components.IconLg
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.components.IconSm
import com.example.chessboard.ui.components.IconXs
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chessboard.R
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.ui.GameEditorMoveSequenceSectionTestTag
import com.example.chessboard.ui.GameEditorNextTestTag
import com.example.chessboard.ui.GameEditorPreviousTestTag
import com.example.chessboard.ui.GameEditorScrollContainerTestTag
import com.example.chessboard.ui.components.AppConfirmDialog
import com.example.chessboard.ui.components.AppDivider
import com.example.chessboard.ui.components.HomeIconButton
import com.example.chessboard.ui.components.BoardActionNavigationBar
import com.example.chessboard.ui.components.BoardActionNavigationItem
import com.example.chessboard.ui.components.AppScreenScaffold
import com.example.chessboard.ui.components.AppTopBar
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingIconInactive
import com.example.chessboard.ui.components.ChessBoardSection
import com.example.chessboard.ui.components.GameMoveTreeSection
import com.example.chessboard.ui.screen.training.DarkInputField
import com.example.chessboard.service.parsePgnMoves
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GameEditorScreenContainer(
    activity: Activity,
    game: GameEntity,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
) {
    val dbProvider = screenContext.inDbProvider
    val gameController = remember { GameController() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(game.id) {
        val parsed = withContext(Dispatchers.Default) { parsePgnMoves(game.pgn) }
        gameController.resetToStartPosition()
        gameController.setOrientation(EditableGameSide.fromSideMask(game.sideMask).orientation)
        gameController.loadFromUciMoves(parsed, targetPly = parsed.size)
        isLoading = false
    }

    GameEditorScreen(
        game = game,
        gameController = gameController,
        isLoading = isLoading,
        onBackClick = screenContext.onBackClick,
        onHomeClick = { screenContext.onNavigate(ScreenType.Home) },
        onNavigate = screenContext.onNavigate,
        onSave = { name, eco, selectedSide ->
            val idx = gameController.currentMoveIndex
            val pgn = gameController.generatePgn(
                event = name.ifBlank { "Opening" },
                upToIndex = idx
            )
            val updatedGame = game.copy(
                event = name.ifBlank { null },
                eco = eco.ifBlank { null },
                pgn = pgn,
                sideMask = selectedSide.sideMask
            )
            val updatedMoves = gameController.getMovesCopy().take(idx)

            (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
                val updated = dbProvider.updateGame(
                    game = updatedGame,
                    moves = updatedMoves
                )

                if (!updated) {
                    return@launch
                }

                withContext(Dispatchers.Main) { screenContext.onBackClick() }
            }
        },
        onDelete = {
            (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
                dbProvider.deleteGame(game.id)
                withContext(Dispatchers.Main) { screenContext.onBackClick() }
            }
        },
        modifier = modifier
    )
}

private fun goToPly(
    gameController: GameController,
    currentPly: Int,
    targetPly: Int
) {
    if (targetPly == currentPly) {
        return
    }

    val safeTargetPly = targetPly.coerceAtLeast(0)
    val diff = safeTargetPly - currentPly
    if (diff > 0) {
        repeat(diff) { gameController.redoMove() }
        return
    }

    repeat(-diff) { gameController.undoMove() }
}

private fun goToStart(
    gameController: GameController,
    currentPly: Int
) {
    if (currentPly == 0) {
        return
    }

    goToPly(
        gameController = gameController,
        currentPly = currentPly,
        targetPly = 0
    )
}


@Composable
private fun GameEditorBoardControlsBar(
    selectedSide: EditableGameSide,
    onSideSelected: (EditableGameSide) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onResetClick: () -> Unit,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoardActionNavigationBar(
        modifier = modifier,
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
                onClick = onResetClick,
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
                modifier = Modifier.testTag(GameEditorPreviousTestTag),
                onClick = onBackClick,
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
                modifier = Modifier.testTag(GameEditorNextTestTag),
                onClick = onForwardClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameEditorScreen(
    game: GameEntity,
    gameController: GameController,
    isLoading: Boolean,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    onSave: (name: String, eco: String, selectedSide: EditableGameSide) -> Unit,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    @Suppress("UNUSED_VARIABLE")
    val boardState = gameController.boardState
    val currentPly = gameController.currentMoveIndex
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editedName by remember(game.id) { mutableStateOf(game.event ?: "") }
    var editedEco by remember(game.id) { mutableStateOf(game.eco ?: "") }
    var selectedSide by remember(game.id) { mutableStateOf(EditableGameSide.fromSideMask(game.sideMask)) }

    if (showDeleteDialog) {
        AppConfirmDialog(
            title = "Delete Opening",
            message = "Delete \"${game.event ?: "this opening"}\"? This cannot be undone.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            confirmText = "Delete",
            isDestructive = true
        )
    }

    AppScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = editedName.ifBlank { "Opening" },
                subtitle = editedEco.ifBlank { null },
                onBackClick = onBackClick,
                actions = {
                    HomeIconButton(onClick = onHomeClick)
                    DeleteIconButton(onClick = { showDeleteDialog = true })
                    IconButton(onClick = { onSave(editedName, editedEco, selectedSide) }) {
                        IconMd(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = TrainingAccentTeal,
                        )
                    }
                }
            )
        },
        bottomBar = {
            GameEditorBoardControlsBar(
                selectedSide = selectedSide,
                onSideSelected = {
                    selectedSide = it
                    gameController.setOrientation(it.orientation)
                },
                canUndo = gameController.canUndo,
                canRedo = gameController.canRedo,
                onResetClick = { goToStart(gameController, currentPly) },
                onBackClick = { gameController.undoMove() },
                onForwardClick = { gameController.redoMove() },
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TrainingAccentTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag(GameEditorScrollContainerTestTag)
                    .verticalScroll(rememberScrollState())
            ) {
                ChessBoardSection(gameController = gameController, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(AppDimens.spaceMd))

                GameMoveTreeSection(
                    importedUciLines = emptyList(),
                    gameController = gameController,
                    modifier = Modifier
                        .padding(horizontal = AppDimens.spaceLg)
                        .testTag(GameEditorMoveSequenceSectionTestTag),
                )

                AppDivider(
                    modifier = Modifier.padding(horizontal = AppDimens.spaceLg, vertical = AppDimens.spaceMd)
                )

                DarkInputField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = "Opening Name",
                    placeholder = "e.g., Sicilian Defense",
                    modifier = Modifier.padding(horizontal = AppDimens.spaceLg)
                )

                Spacer(modifier = Modifier.height(AppDimens.spaceMd))

                DarkInputField(
                    value = editedEco,
                    onValueChange = { editedEco = it },
                    label = "ECO Code",
                    placeholder = "e.g., B20",
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(start = AppDimens.spaceLg)
                )

                Spacer(modifier = Modifier.height(AppDimens.spaceXl))
            }
        }
    }
}
