package com.example.chessboard.ui.screen.createOpening

/**
 * Stateful container for the create-opening screen.
 *
 * Keep in this file:
 * - screen-level state
 * - wiring between UI callbacks and services
 * - import/save orchestration needed to prepare data for the UI and post-save flow
 * - navigation callbacks owned by the screen container
 *
 * It is acceptable to add here:
 * - additional screen state fields
 * - state transformations for this screen
 * - calls that prepare data snapshots before passing control to services or dialogs
 *
 * Do not add here:
 * - large chunks of presentational UI markup that belong in CreateOpeningScreen.kt
 * - reusable post-save training/template logic that belongs in CreateOpeningPostSaveFlow.kt
 * - generic repository or service code unrelated to this screen container
 */
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.chessboard.boardmodel.GameController
import com.example.chessboard.entity.GameEntity
import com.example.chessboard.service.buildStoredPgnFromUci
import com.example.chessboard.service.extractPgnHeaders
import com.example.chessboard.service.parsePgnToUciLines
import com.example.chessboard.service.uciMovesToMoves
import com.example.chessboard.ui.screen.EditableGameSide
import com.example.chessboard.ui.screen.ScreenContainerContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateOpeningScreenContainer(
    activity: Activity,
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
) {
    val dbProvider = screenContext.inDbProvider
    val gameController = remember { GameController() }
    var selectedSide by remember { mutableStateOf(EditableGameSide.AS_WHITE) }
    var openingName by remember { mutableStateOf("") }
    var ecoCode by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var pgnText by remember { mutableStateOf("") }
    var pgnImportError by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var postSaveState by remember { mutableStateOf(CreateOpeningPostSaveState()) }
    var importedPgnHeaders by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var importedUciLines by remember { mutableStateOf<List<List<String>>>(emptyList()) }

    LaunchedEffect(selectedSide) {
        gameController.setOrientation(selectedSide.orientation)
    }

    CreateOpeningPostSaveDialogs(
        activity = activity,
        dbProvider = dbProvider,
        state = postSaveState,
        onStateChange = { postSaveState = it },
        onFinished = {
            postSaveState = CreateOpeningPostSaveState()
            screenContext.onBackClick()
        },
        onError = { message ->
            postSaveState = CreateOpeningPostSaveState()
            saveError = message
        },
    )

    CreateOpeningScreen(
        gameController = gameController,
        selectedSide = selectedSide,
        onSideSelected = { selectedSide = it },
        onBackClick = screenContext.onBackClick,
        openingName = openingName,
        onOpeningNameChange = { openingName = it; nameError = false },
        ecoCode = ecoCode,
        onEcoCodeChange = { ecoCode = it },
        nameError = nameError,
        pgnText = pgnText,
        onPgnTextChange = {
            pgnText = it
            importedPgnHeaders = emptyMap()
            importedUciLines = emptyList()
        },
        importedUciLines = importedUciLines,
        pgnImportError = pgnImportError,
        onPgnImportErrorDismiss = { pgnImportError = null },
        saveError = saveError,
        onSaveErrorDismiss = { saveError = null },
        onImportPgnClick = {
            (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.Default) {
                try {
                    val headers = extractPgnHeaders(pgnText)
                    val uciLines = parsePgnToUciLines(pgnText)
                    withContext(Dispatchers.Main) {
                        if (uciLines.isEmpty()) {
                            pgnImportError = "No valid moves found in PGN text"
                        } else {
                            headers["Event"]
                                ?.takeIf { it.isNotBlank() && it != "?" }
                                ?.let { openingName = it }
                            headers["ECO"]
                                ?.takeIf { it.isNotBlank() && it != "?" }
                                ?.let { ecoCode = it }
                            importedPgnHeaders = headers
                            importedUciLines = uciLines
                            gameController.loadFromUciMoves(uciLines.first())
                            pgnImportError = null
                        }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        pgnImportError = "Failed to parse PGN"
                    }
                }
            }
        },
        onSave = {
            if (openingName.isBlank()) {
                nameError = true
                return@CreateOpeningScreen
            }

            val importedHeadersSnapshot = importedPgnHeaders
            val importedLinesSnapshot = importedUciLines
            val openingNameSnapshot = openingName
            val ecoCodeSnapshot = ecoCode
            val selectedSideSnapshot = selectedSide
            val movesSnapshot = gameController.getMovesCopy()
            val generatedPgnSnapshot = if (importedLinesSnapshot.isEmpty()) {
                gameController.generatePgn(event = openingNameSnapshot.ifBlank { "Opening" })
            } else {
                null
            }

            (activity as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
                val savedGameIds = if (importedLinesSnapshot.isEmpty()) {
                    val entity = GameEntity(
                        event = openingNameSnapshot.ifBlank { null },
                        eco = ecoCodeSnapshot.ifBlank { null },
                        pgn = generatedPgnSnapshot ?: "",
                        initialFen = "",
                        sideMask = selectedSideSnapshot.sideMask,
                    )
                    listOfNotNull(dbProvider.addGameAndGetId(entity, movesSnapshot))
                } else {
                    buildList {
                        importedLinesSnapshot.forEachIndexed { index, uciMoves ->
                            val eventName = buildImportedLineEventName(
                                baseName = openingNameSnapshot,
                                index = index,
                                total = importedLinesSnapshot.size,
                            )
                            val entity = GameEntity(
                                white = importedHeadersSnapshot["White"]?.takeIf { it.isNotBlank() && it != "?" },
                                black = importedHeadersSnapshot["Black"]?.takeIf { it.isNotBlank() && it != "?" },
                                result = importedHeadersSnapshot["Result"]?.takeIf { it.isNotBlank() && it != "?" },
                                site = importedHeadersSnapshot["Site"]?.takeIf { it.isNotBlank() && it != "?" },
                                round = importedHeadersSnapshot["Round"]?.takeIf { it.isNotBlank() && it != "?" },
                                event = eventName,
                                eco = ecoCodeSnapshot.ifBlank { null },
                                pgn = buildStoredPgnFromUci(
                                    uciMoves = uciMoves,
                                    event = eventName ?: "Opening",
                                    whiteName = importedHeadersSnapshot["White"]?.takeIf { it.isNotBlank() && it != "?" } ?: "White",
                                    blackName = importedHeadersSnapshot["Black"]?.takeIf { it.isNotBlank() && it != "?" } ?: "Black",
                                ),
                                initialFen = "",
                                sideMask = selectedSideSnapshot.sideMask,
                            )
                            dbProvider.addGameAndGetId(entity, uciMovesToMoves(uciMoves))?.let { gameId ->
                                add(gameId)
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (savedGameIds.isNotEmpty()) {
                        postSaveState = startCreateOpeningPostSaveFlow(
                            openingName = openingNameSnapshot,
                            savedGameIds = savedGameIds,
                        )
                    } else {
                        saveError = if (importedLinesSnapshot.isEmpty()) {
                            "Failed to save opening"
                        } else {
                            "None of the imported lines could be saved"
                        }
                    }
                }
            }
        },
        modifier = modifier,
    )
}
