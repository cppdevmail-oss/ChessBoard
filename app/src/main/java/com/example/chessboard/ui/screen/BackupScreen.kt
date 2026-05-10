package com.example.chessboard.ui.screen

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.chessboard.service.LineBackupRestoreProgress
import com.example.chessboard.service.LineBackupRestoreResult
import com.example.chessboard.ui.BackupRestoreCancelTestTag
import com.example.chessboard.ui.BackupRestoreProgressDialogTestTag
import com.example.chessboard.ui.components.AppBottomNavigation
import com.example.chessboard.ui.components.AppConfirmDialog
import com.example.chessboard.ui.components.AppMessageDialog
import com.example.chessboard.ui.components.AppScreenScaffold
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.AppTopBar
import com.example.chessboard.ui.components.BodySecondaryText
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.PrimaryButton
import com.example.chessboard.ui.components.ScreenSection
import com.example.chessboard.ui.components.ScreenTitleText
import com.example.chessboard.ui.components.defaultAppBottomNavigationItems
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

typealias BackupRestoreRunner = suspend (
    uri: Uri,
    onProgress: suspend (LineBackupRestoreProgress) -> Unit,
) -> LineBackupRestoreResult

@Composable
fun BackupScreenContainer(
    activity: Activity,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
    testRestoreUri: Uri? = null,
    restoreBackupRunner: BackupRestoreRunner? = null,
) {
    val lineBackupService = remember { screenContext.inDbProvider.createLineBackupService() }

    fun resolveDefaultBackupFileName(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US)
        val timestamp = formatter.format(Date())
        return "lines-backup-$timestamp.pgn"
    }

    fun ensureBackupFileName(fileName: String): String {
        val trimmed = fileName.trim().ifBlank { resolveDefaultBackupFileName() }
        if (trimmed.endsWith(".pgn", ignoreCase = true)) {
            return trimmed
        }

        return "$trimmed.pgn"
    }

    fun resolveRestoreMessage(result: LineBackupRestoreResult): String {
        if (result.restoredLinesCount == 0 && result.skippedLinesCount == 0) {
            return "No lines were found in the selected backup."
        }

        return buildString {
            appendLine("Restored lines: ${result.restoredLinesCount}")
            append("Skipped lines: ${result.skippedLinesCount}")
        }
    }

    fun resolveRestoreCanceledMessage(progress: LineBackupRestoreProgress?): String {
        val currentProgress = progress ?: return "Restore canceled."

        return buildString {
            appendLine("Restore canceled.")
            appendLine("Processed lines: ${currentProgress.processedLinesCount}/${currentProgress.totalLines}")
            appendLine("Restored lines: ${currentProgress.restoredLinesCount}")
            append("Skipped lines: ${currentProgress.skippedLinesCount}")
        }
    }

    suspend fun runRestoreBackup(
        restoreUri: Uri,
        onProgress: suspend (LineBackupRestoreProgress) -> Unit,
    ): LineBackupRestoreResult {
        if (restoreBackupRunner != null) {
            return restoreBackupRunner(restoreUri, onProgress)
        }

        val inputStream = activity.contentResolver.openInputStream(restoreUri)
        if (inputStream == null) {
            throw IllegalStateException("Failed to open the selected backup file")
        }

        return inputStream.use { stream ->
            lineBackupService.restoreBackup(stream, onProgress)
        }
    }

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupFileName by remember { mutableStateOf(resolveDefaultBackupFileName()) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var backupError by remember { mutableStateOf<String?>(null) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var restoreProgress by remember { mutableStateOf<LineBackupRestoreProgress?>(null) }
    var restoreJob by remember { mutableStateOf<Job?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-chess-pgn")
    ) { uri: Uri? ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
            try {
                val outputStream = activity.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    withContext(Dispatchers.Main) {
                        backupError = "Failed to open the selected destination"
                    }
                    return@launch
                }

                outputStream.use { stream ->
                    lineBackupService.writeBackup(stream)
                }

                withContext(Dispatchers.Main) {
                    backupMessage = "Backup saved as ${ensureBackupFileName(backupFileName)}"
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    backupError = error.message ?: "Failed to save backup"
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        pendingRestoreUri = uri
    }

    if (backupMessage != null) {
        AppMessageDialog(
            title = "Backup Saved",
            message = backupMessage!!,
            onDismiss = { backupMessage = null }
        )
    }

    if (backupError != null) {
        AppMessageDialog(
            title = "Backup Failed",
            message = backupError!!,
            onDismiss = { backupError = null }
        )
    }

    if (restoreMessage != null) {
        AppMessageDialog(
            title = "Restore Lines",
            message = restoreMessage!!,
            onDismiss = { restoreMessage = null }
        )
    }

    if (restoreError != null) {
        AppMessageDialog(
            title = "Restore Failed",
            message = restoreError!!,
            onDismiss = { restoreError = null }
        )
    }

    if (restoreProgress != null) {
        BackupRestoreProgressDialog(
            progress = restoreProgress!!,
            onCancel = {
                restoreJob?.cancel()
            }
        )
    }

    if (pendingRestoreUri != null) {
        AppConfirmDialog(
            title = "Restore Lines",
            message = "Restore lines from the selected backup file?",
            onDismiss = { pendingRestoreUri = null },
            onConfirm = {
                val restoreUri = pendingRestoreUri!!
                pendingRestoreUri = null

                val lifecycleOwner = activity as? LifecycleOwner ?: return@AppConfirmDialog
                restoreJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val result = runRestoreBackup(restoreUri) { progress ->
                            withContext(Dispatchers.Main) {
                                restoreProgress = progress
                            }
                        }

                        withContext(Dispatchers.Main) {
                            restoreProgress = null
                            restoreJob = null
                            restoreMessage = resolveRestoreMessage(result)
                        }
                    } catch (_: CancellationException) {
                        withContext(NonCancellable + Dispatchers.Main) {
                            restoreJob = null
                            restoreMessage = resolveRestoreCanceledMessage(restoreProgress)
                            restoreProgress = null
                        }
                    } catch (error: Exception) {
                        withContext(Dispatchers.Main) {
                            restoreProgress = null
                            restoreJob = null
                            restoreError = error.message ?: "Failed to restore lines"
                        }
                    }
                }
            },
            confirmText = "Restore",
            isDestructive = true
        )
    }

    if (showBackupDialog) {
        BackupFileNameDialog(
            fileName = backupFileName,
            onFileNameChange = { backupFileName = it },
            onDismiss = { showBackupDialog = false },
            onConfirm = {
                val resolvedName = ensureBackupFileName(backupFileName)
                backupFileName = resolvedName
                showBackupDialog = false
                backupLauncher.launch(resolvedName)
            }
        )
    }

    BackupScreen(
        onBackClick = screenContext.onBackClick,
        onNavigate = screenContext.onNavigate,
        onCreateBackupClick = {
            backupFileName = resolveDefaultBackupFileName()
            showBackupDialog = true
        },
        onRestoreLinesClick = {
            if (testRestoreUri != null) {
                pendingRestoreUri = testRestoreUri
                return@BackupScreen
            }

            restoreLauncher.launch(arrayOf("application/x-chess-pgn", "text/plain", "*/*"))
        },
        modifier = modifier
    )
}

@Composable
private fun BackupScreen(
    onBackClick: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    onCreateBackupClick: () -> Unit = {},
    onRestoreLinesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = "Backup",
                subtitle = "Export and restore saved lines",
                onBackClick = onBackClick,
                filledBackButton = true,
            )
        },
        bottomBar = {
            AppBottomNavigation(
                items = defaultAppBottomNavigationItems(),
                selectedItem = ScreenType.Home,
                onItemSelected = onNavigate,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppDimens.spaceLg),
            verticalArrangement = Arrangement.Center,
        ) {
            ScreenSection {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg),
                ) {
                    ScreenTitleText(text = "Line Backup")
                    BodySecondaryText(text = "Create a PGN backup or restore lines later.")
                    PrimaryButton(
                        text = "Create Backup",
                        onClick = onCreateBackupClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrimaryButton(
                        text = "Restore Lines",
                        onClick = onRestoreLinesClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupFileNameDialog(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            ScreenTitleText(text = "Backup Lines")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)) {
                BodySecondaryText(
                    text = "Choose a file name. On the next step Android will ask where to save the backup file."
                )
                AppTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = "File name",
                    placeholder = "lines-backup.pgn"
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Location",
                onClick = onConfirm
            )
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Background.SurfaceDark)
            ) {
                CardMetaText(text = "Cancel")
            }
        },
        containerColor = Background.ScreenDark,
    )
}

@Composable
private fun BackupRestoreProgressDialog(
    progress: LineBackupRestoreProgress,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag(BackupRestoreProgressDialogTestTag),
        onDismissRequest = {},
        title = {
            ScreenTitleText(text = "Restoring Lines")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spaceSm)) {
                BodySecondaryText(text = "Total lines: ${progress.totalLines}")
                BodySecondaryText(
                    text = "Processed lines: ${progress.processedLinesCount}/${progress.totalLines}"
                )
                BodySecondaryText(text = "Restored lines: ${progress.restoredLinesCount}")
                BodySecondaryText(text = "Skipped lines: ${progress.skippedLinesCount}")
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Stop",
                onClick = onCancel,
                modifier = Modifier.testTag(BackupRestoreCancelTestTag)
            )
        },
        containerColor = Background.ScreenDark,
    )
}
