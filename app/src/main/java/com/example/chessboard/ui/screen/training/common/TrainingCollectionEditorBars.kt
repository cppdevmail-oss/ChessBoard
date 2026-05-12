package com.example.chessboard.ui.screen.training.common

/*
 * Shared top and bottom action-bar factory for training collection editors.
 *
 * Keep the common editor actions and optional training-specific add-ons here so
 * edit-training and edit-template screens can assemble near-identical bars
 * without duplicating button lists. Do not add screen state or persistence.
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.chessboard.ui.components.BoardActionNavigationBar
import com.example.chessboard.ui.components.BoardActionNavigationItem
import com.example.chessboard.ui.components.HomeIconButton
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.theme.BottomBarContentColor

private data class TrainingCollectionEditorTopBarAction(
    val content: @Composable () -> Unit,
)

private data class TrainingCollectionEditorBottomBarAction(
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
    val content: @Composable (Boolean) -> Unit,
)

internal class TrainingCollectionEditorBarsFactory(
    onHomeClick: () -> Unit,
    hasSelection: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    deleteContentDescription: String,
    canUndo: Boolean,
    onPrevClick: () -> Unit,
    canRedo: Boolean,
    onNextClick: () -> Unit,
) {
    private val topBarActions = mutableListOf(
        createHomeTopBarAction(onClick = onHomeClick),
    )
    private val bottomBarActions = mutableListOf(
        createEditBottomBarAction(
            enabled = hasSelection,
            onClick = onEditClick,
        ),
        createDeleteBottomBarAction(
            enabled = hasSelection,
            onClick = onDeleteClick,
            contentDescription = deleteContentDescription,
        ),
        createPrevBottomBarAction(
            enabled = canUndo,
            onClick = onPrevClick,
        ),
        createNextBottomBarAction(
            enabled = canRedo,
            onClick = onNextClick,
        ),
    )

    fun addTopBarAction(
        index: Int = -1,
        content: @Composable () -> Unit,
    ): TrainingCollectionEditorBarsFactory {
        topBarActions.insertAt(
            index = index,
            value = TrainingCollectionEditorTopBarAction(content = content),
        )

        return this
    }

    fun addBottomBarAction(
        label: String,
        enabled: Boolean,
        onClick: () -> Unit,
        index: Int = -1,
        content: @Composable (Boolean) -> Unit,
    ): TrainingCollectionEditorBarsFactory {
        bottomBarActions.insertAt(
            index = index,
            value = TrainingCollectionEditorBottomBarAction(
                label = label,
                enabled = enabled,
                onClick = onClick,
                content = content,
            ),
        )

        return this
    }

    fun buildTopBarActions(): @Composable () -> Unit {
        val actions = topBarActions.toList()
        return {
            TrainingCollectionEditorTopBarActions(actions = actions)
        }
    }

    fun buildBottomBar(): @Composable () -> Unit {
        val actions = bottomBarActions.toList()
        return {
            TrainingCollectionEditorBottomBar(actions = actions)
        }
    }
}

private fun <T> MutableList<T>.insertAt(index: Int, value: T) {
    if (index in 0..size) {
        add(index, value)
        return
    }

    add(value)
}

@Composable
private fun TrainingCollectionEditorTopBarActions(
    actions: List<TrainingCollectionEditorTopBarAction>,
) {
    actions.forEach { action ->
        action.content()
    }
}

@Composable
private fun TrainingCollectionEditorBottomBar(
    actions: List<TrainingCollectionEditorBottomBarAction>,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5,
) {
    BoardActionNavigationBar(
        modifier = modifier,
        maxVisibleItems = maxVisibleItems,
        items = actions.map { action ->
            BoardActionNavigationItem(
                label = action.label,
                enabled = action.enabled,
                onClick = action.onClick,
            ) {
                action.content(action.enabled)
            }
        },
    )
}

private fun createHomeTopBarAction(
    onClick: () -> Unit,
    contentDescription: String = "Home",
): TrainingCollectionEditorTopBarAction {
    return TrainingCollectionEditorTopBarAction {
        HomeIconButton(
            onClick = onClick,
            contentDescription = contentDescription,
        )
    }
}

private fun createEditBottomBarAction(
    enabled: Boolean,
    onClick: () -> Unit,
): TrainingCollectionEditorBottomBarAction {
    return TrainingCollectionEditorBottomBarAction(
        label = "Edit",
        enabled = enabled,
        onClick = onClick,
    ) { isEnabled ->
        IconMd(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit line",
            tint = resolveTrainingCollectionEditorActionTint(isEnabled),
        )
    }
}

private fun createDeleteBottomBarAction(
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
): TrainingCollectionEditorBottomBarAction {
    return TrainingCollectionEditorBottomBarAction(
        label = "Delete",
        enabled = enabled,
        onClick = onClick,
    ) { isEnabled ->
        IconMd(
            imageVector = Icons.Default.Delete,
            contentDescription = contentDescription,
            tint = resolveTrainingCollectionEditorActionTint(isEnabled),
        )
    }
}

private fun createPrevBottomBarAction(
    enabled: Boolean,
    onClick: () -> Unit,
): TrainingCollectionEditorBottomBarAction {
    return TrainingCollectionEditorBottomBarAction(
        label = "Back",
        enabled = enabled,
        onClick = onClick,
    ) { isEnabled ->
        IconMd(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous move",
            tint = resolveTrainingCollectionEditorActionTint(isEnabled),
        )
    }
}

private fun createNextBottomBarAction(
    enabled: Boolean,
    onClick: () -> Unit,
): TrainingCollectionEditorBottomBarAction {
    return TrainingCollectionEditorBottomBarAction(
        label = "Forward",
        enabled = enabled,
        onClick = onClick,
    ) { isEnabled ->
        IconMd(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next move",
            tint = resolveTrainingCollectionEditorActionTint(isEnabled),
        )
    }
}

private fun resolveTrainingCollectionEditorActionTint(enabled: Boolean): Color {
    if (enabled) {
        return BottomBarContentColor
    }

    return BottomBarContentColor.copy(alpha = 0.5f)
}
