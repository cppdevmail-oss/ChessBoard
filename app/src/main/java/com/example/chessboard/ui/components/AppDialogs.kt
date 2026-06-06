package com.example.chessboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.chessboard.R
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.ButtonColor
import com.example.chessboard.ui.theme.TextColor

data class AppMessageDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val testTag: String? = null,
)

/** Displays a standard app dialog for informational messages and simple actions. */
@Composable
fun AppMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    actions: List<AppMessageDialogAction>? = null,
    messageModifier: Modifier = Modifier,
) {
    RenderAppMessageDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        modifier = modifier,
        confirmText = stringResource(R.string.common_ok),
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismissClick = onDismissClick,
        actions = actions,
        messageModifier = messageModifier,
    )
}

/** Displays a standard app dialog for informational messages and simple actions. */
@Composable
fun AppMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null,
    actions: List<AppMessageDialogAction>? = null,
    messageModifier: Modifier = Modifier,
) {
    RenderAppMessageDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        modifier = modifier,
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismissClick = onDismissClick,
        actions = actions,
        messageModifier = messageModifier,
    )
}

@Composable
private fun RenderAppMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier,
    confirmText: String,
    onConfirm: (() -> Unit)?,
    dismissText: String?,
    onDismissClick: (() -> Unit)?,
    actions: List<AppMessageDialogAction>?,
    messageModifier: Modifier,
) {
    @Composable
    fun RenderMessageDialogConfirmButton() {

        @Composable
        fun RenderMessageDialogActionButtons(
            actions: List<AppMessageDialogAction>
        ) {
            fun resolveMessageDialogActionModifier(
                testTag: String?
            ): Modifier {
                if (testTag == null) {
                    return Modifier
                }

                return Modifier.testTag(testTag)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceXs)
            ) {
                actions.forEach { action ->
                    TextButton(
                        onClick = action.onClick,
                        modifier = resolveMessageDialogActionModifier(action.testTag),
                    ) {
                        BodySecondaryText(
                            text = action.text,
                            color = TextColor.Primary
                        )
                    }
                }
            }
        }

        if (!actions.isNullOrEmpty()) {
            RenderMessageDialogActionButtons(actions)
            return
        }

        TextButton(
            onClick = resolveMessageDialogConfirmAction(
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        ) {
            BodySecondaryText(
                text = confirmText,
                color = TextColor.Primary
            )
        }
    }

    @Composable
    fun RenderMessageDialogDismissArea() {
        if (!actions.isNullOrEmpty()) {
            return
        }

        RenderMessageDialogDismissButton(
            dismissText = dismissText,
            onDismiss = onDismiss,
            onDismissClick = onDismissClick
        )
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        containerColor = Background.ScreenDark,
        title = {
            SectionTitleText(text = title)
        },
        text = {
            BodySecondaryText(text = message, modifier = messageModifier)
        },
        confirmButton = {
            RenderMessageDialogConfirmButton()
        },
        dismissButton = {
            RenderMessageDialogDismissArea()
        }
    )
}

/** Displays a standard app confirmation dialog for irreversible or important actions. */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonModifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    RenderAppConfirmDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        modifier = modifier,
        confirmText = stringResource(R.string.common_confirm),
        confirmButtonModifier = confirmButtonModifier,
        dismissText = stringResource(R.string.common_cancel),
        isDestructive = isDestructive,
    )
}

/** Displays a standard app confirmation dialog for irreversible or important actions. */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String,
    confirmButtonModifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    RenderAppConfirmDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        modifier = modifier,
        confirmText = confirmText,
        confirmButtonModifier = confirmButtonModifier,
        dismissText = stringResource(R.string.common_cancel),
        isDestructive = isDestructive,
    )
}

/** Displays a standard app confirmation dialog for irreversible or important actions. */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String,
    confirmButtonModifier: Modifier = Modifier,
    dismissText: String,
    isDestructive: Boolean = false,
) {
    RenderAppConfirmDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        modifier = modifier,
        confirmText = confirmText,
        confirmButtonModifier = confirmButtonModifier,
        dismissText = dismissText,
        isDestructive = isDestructive,
    )
}

@Composable
private fun RenderAppConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier,
    confirmText: String,
    confirmButtonModifier: Modifier,
    dismissText: String,
    isDestructive: Boolean,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        containerColor = Background.ScreenDark,
        title = {
            ScreenTitleText(text = title)
        },
        text = {
            BodySecondaryText(text = message)
        },
        confirmButton = {
            PrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = confirmButtonModifier,
                containerColor = resolveConfirmDialogContainerColor(
                    isDestructive = isDestructive
                )
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                CardMetaText(text = dismissText)
            }
        }
    )
}

@Composable
private fun RenderMessageDialogDismissButton(
    dismissText: String?,
    onDismiss: () -> Unit,
    onDismissClick: (() -> Unit)?
) {
    if (dismissText == null) {
        return
    }

    TextButton(
        onClick = resolveMessageDialogDismissAction(
            onDismiss = onDismiss,
            onDismissClick = onDismissClick
        )
    ) {
        BodySecondaryText(
            text = dismissText,
            color = TextColor.Primary
        )
    }
}

private fun resolveMessageDialogConfirmAction(
    onConfirm: (() -> Unit)?,
    onDismiss: () -> Unit
): () -> Unit {
    if (onConfirm != null) {
        return onConfirm
    }

    return onDismiss
}

private fun resolveMessageDialogDismissAction(
    onDismiss: () -> Unit,
    onDismissClick: (() -> Unit)?
): () -> Unit {
    if (onDismissClick != null) {
        return onDismissClick
    }

    return onDismiss
}

private fun resolveConfirmDialogContainerColor(
    isDestructive: Boolean
): androidx.compose.ui.graphics.Color {
    if (isDestructive) {
        return ButtonColor.DestructiveContainer
    }

    return ButtonColor.PrimaryContainer
}

/** Displays a blocking loading dialog for short async work triggered from the UI. */
@Composable
fun AppLoadingDialog(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        containerColor = Background.ScreenDark,
        title = {
            SectionTitleText(text = title)
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spaceMd),
            ) {
                CircularProgressIndicator(color = TrainingAccentTeal)
                BodySecondaryText(
                    text = message,
                    modifier = Modifier.padding(top = AppDimens.spaceXs),
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}
