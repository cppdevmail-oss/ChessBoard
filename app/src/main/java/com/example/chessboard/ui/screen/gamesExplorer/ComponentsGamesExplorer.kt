package com.example.chessboard.ui.screen.gamesExplorer

/**
 * Visual blocks and search helpers for the games-explorer package.
 *
 * Keep in this file:
 * - render-only components used by the games explorer screen
 * - search dialog UI and filter matching helpers for games explorer
 * - small package-local models that support the explorer UI
 *
 * It is acceptable to add here:
 * - new reusable UI blocks for the games explorer package
 * - search-related helpers used only by files in this package
 *
 * Do not add here:
 * - database calls, coroutine orchestration, or navigation decisions
 * - logic for unrelated screens
 * - broad app-wide UI utilities
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.service.ParsedGame
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.BoardActionNavigationBar
import com.example.chessboard.ui.components.BoardActionNavigationItem
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.CardSurface
import com.example.chessboard.ui.components.GameMoveTreeSection
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.components.PrimaryButton
import com.example.chessboard.ui.components.SectionTitleText
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TextColor
import com.example.chessboard.ui.theme.TrainingIconInactive

internal data class GamesExplorerFilterState(
    val query: String = "",
    val isCaseSensitive: Boolean = false
)

@Composable
internal fun GameBlock(
    parsedGame: ParsedGame,
    isSelected: Boolean,
    gameController: GameController,
    onSelectClick: () -> Unit,
    onMovePlyClick: (ply: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        GameMoveTreeSection(
            importedUciLines = listOf(parsedGame.uciMoves),
            gameController = gameController,
            modifier = modifier.fillMaxWidth(),
            onMoveSelected = { _, targetPly -> onMovePlyClick(targetPly) },
        )
        return
    }

    CardSurface(
        modifier = modifier.fillMaxWidth(),
        color = Background.SurfaceDark,
        border = null,
        onClick = onSelectClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionTitleText(
                    text = parsedGame.game.event ?: "Opening"
                )
                GameBlockMetaRow(
                    eco = parsedGame.game.eco,
                    gameId = parsedGame.game.id
                )
            }
            CardMetaText(text = "${parsedGame.moveLabels.size} moves")
        }
    }
}

@Composable
internal fun GamesExplorerBoardControlsBar(
    canUndo: Boolean,
    canRedo: Boolean,
    hasSelection: Boolean,
    simpleViewEnabled: Boolean = false,
    onPrevClick: () -> Unit,
    onResetClick: () -> Unit,
    onNextClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onCloneClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    BoardActionNavigationBar(
        maxVisibleItems = if (simpleViewEnabled) 4 else 7,
        items = if (simpleViewEnabled) {
            listOf(
                BoardActionNavigationItem(
                    label = "Edit",
                    enabled = hasSelection,
                    onClick = onEditClick,
                ) {
                    IconMd(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit game",
                        tint = resolveGamesExplorerActionTint(hasSelection),
                    )
                },
                BoardActionNavigationItem(
                    label = "Delete",
                    enabled = hasSelection,
                    onClick = onDeleteClick,
                ) {
                    IconMd(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete game",
                        tint = resolveGamesExplorerActionTint(hasSelection),
                    )
                },
                BoardActionNavigationItem(
                    label = "Back",
                    enabled = canUndo,
                    onClick = onPrevClick,
                ) {
                    IconMd(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = resolveGamesExplorerActionTint(canUndo),
                    )
                },
                BoardActionNavigationItem(
                    label = "Forward",
                    enabled = canRedo,
                    onClick = onNextClick,
                ) {
                    IconMd(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = resolveGamesExplorerActionTint(canRedo),
                    )
                },
            )
        } else listOf(
            BoardActionNavigationItem(
                label = "Reset",
                enabled = hasSelection,
                onClick = onResetClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = resolveGamesExplorerActionTint(hasSelection),
                )
            },
            BoardActionNavigationItem(
                label = "Analyze",
                enabled = hasSelection,
                onClick = onAnalyzeClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analyze game",
                    tint = resolveGamesExplorerActionTint(hasSelection),
                )
            },
            BoardActionNavigationItem(
                label = "Clone",
                enabled = hasSelection,
                onClick = onCloneClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Clone game",
                    tint = resolveGamesExplorerActionTint(hasSelection),
                )
            },
            BoardActionNavigationItem(
                label = "Edit",
                enabled = hasSelection,
                onClick = onEditClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit game",
                    tint = resolveGamesExplorerActionTint(hasSelection),
                )
            },
            BoardActionNavigationItem(
                label = "Delete",
                enabled = hasSelection,
                onClick = onDeleteClick,
            ) {
                IconMd(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete game",
                    tint = resolveGamesExplorerActionTint(hasSelection),
                )
            },
            BoardActionNavigationItem(
                label = "Back",
                enabled = canUndo,
                onClick = onPrevClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = resolveGamesExplorerActionTint(canUndo),
                )
            },
            BoardActionNavigationItem(
                label = "Forward",
                enabled = canRedo,
                onClick = onNextClick,
            ) {
                IconMd(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = resolveGamesExplorerActionTint(canRedo),
                )
            },
        ),
    )
}

private fun resolveGamesExplorerActionTint(isEnabled: Boolean): Color {
    if (isEnabled) {
        return TrainingIconInactive
    }

    return TrainingIconInactive.copy(alpha = 0.5f)
}

@Composable
private fun GameBlockMetaRow(
    eco: String?,
    gameId: Long
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!eco.isNullOrBlank()) {
            CardMetaText(text = eco)
        }

        CardMetaText(text = "ID: $gameId")
    }
}

@Composable
internal fun RenderGamesExplorerSearchDialog(
    visible: Boolean,
    filterState: GamesExplorerFilterState,
    onDismiss: () -> Unit,
    onFilterStateChange: (GamesExplorerFilterState) -> Unit,
    onApplyClick: () -> Unit
) {
    if (!visible) {
        return
    }

    fun updateQuery(query: String) {
        onFilterStateChange(
            filterState.copy(query = query)
        )
    }

    fun updateCaseSensitive(isCaseSensitive: Boolean) {
        onFilterStateChange(
            filterState.copy(isCaseSensitive = isCaseSensitive)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background.ScreenDark,
        title = {
            SectionTitleText(text = "Search Games")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)
            ) {
                AppTextField(
                    value = filterState.query,
                    onValueChange = ::updateQuery,
                    label = "Game name",
                    placeholder = "Enter part of the title"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Case sensitive",
                            color = TextColor.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        CardMetaText(
                            text = "Match uppercase and lowercase exactly"
                        )
                    }
                    Checkbox(
                        checked = filterState.isCaseSensitive,
                        onCheckedChange = ::updateCaseSensitive
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Apply",
                onClick = onApplyClick
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                CardMetaText(text = "Cancel")
            }
        }
    )
}
