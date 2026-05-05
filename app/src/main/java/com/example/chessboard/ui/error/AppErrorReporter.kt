package com.example.chessboard.ui.error

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val AppErrorLogTag = "ChessBoardAppError"

const val DefaultUnexpectedErrorMessage = "Something went wrong. Please try again."

data class AppErrorUiState(
    val title: String = "Unexpected Error",
    val message: String = DefaultUnexpectedErrorMessage,
)

class AppErrorReporter(
    private val onReport: (AppErrorUiState) -> Unit,
) {
    fun report(
        error: Throwable,
        message: String = DefaultUnexpectedErrorMessage,
    ) {
        if (error is CancellationException) {
            throw error
        }

        Log.e(AppErrorLogTag, message, error)
        onReport(AppErrorUiState(message = message))
    }

    companion object {
        val NoOp = AppErrorReporter {}
    }
}

fun CoroutineScope.launchAppCatching(
    errorReporter: AppErrorReporter,
    message: String = DefaultUnexpectedErrorMessage,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return launch {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            errorReporter.report(error, message)
        }
    }
}
