package com.example.chessboard.ui.screen.training.common

/*
 * Shared remove-from-collection action for training-like editors.
 *
 * Keep only the top-bar cut action and confirmation dialog here so training
 * and template editors can share the same remove-from-collection UI. Do not
 * add collection-specific save logic or screen-level orchestration here.
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chessboard.ui.components.AppConfirmDialog
import com.example.chessboard.ui.components.IconMd
import com.example.chessboard.ui.theme.TextColor

@Composable
internal fun TrainingCollectionRemoveAction(
    selectedLine: TrainingLineEditorItem?,
    collectionLabel: String,
    onConfirmRemove: (Long) -> Unit,
) {
    var lineToRemove by remember { mutableStateOf<TrainingLineEditorItem?>(null) }

    if (lineToRemove != null) {
        AppConfirmDialog(
            title = "Remove Line",
            message = "Remove \"${lineToRemove!!.title}\" from $collectionLabel?",
            onDismiss = { lineToRemove = null },
            onConfirm = {
                val lineId = lineToRemove!!.lineId
                lineToRemove = null
                onConfirmRemove(lineId)
            },
            confirmText = "Remove",
            isDestructive = true,
        )
    }

    if (selectedLine == null) {
        return
    }

    IconButton(onClick = { lineToRemove = selectedLine }) {
        IconMd(
            imageVector = Icons.Default.ContentCut,
            contentDescription = "Remove line from $collectionLabel",
            tint = TextColor.Primary,
        )
    }
}
