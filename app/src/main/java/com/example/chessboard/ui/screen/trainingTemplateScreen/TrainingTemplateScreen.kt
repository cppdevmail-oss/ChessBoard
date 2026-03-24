package com.example.chessboard.ui.screen.trainingTemplateScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrainingTemplateScreen(
    modifier: Modifier = Modifier
) {
    // ----------------------
    // STATE (пока локально)
    // TODO: перенести во ViewModel
    // ----------------------

    var templateName by remember { mutableStateOf("") }

    var selectedSide by remember { mutableStateOf("WHITE") }
    var rangeStart by remember { mutableStateOf(0) }

    val candidateGames = remember { mutableStateListOf<GameUiModel>() }
    val selectedGames = remember { mutableStateListOf<TemplateGameUiModel>() }

    // TODO: загрузка кандидатов из БД при старте и изменении фильтров

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ----------------------
        // TEMPLATE NAME
        // ----------------------

        OutlinedTextField(
            value = templateName,
            onValueChange = { templateName = it },
            label = { Text("Название шаблона") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------
        // SEARCH SETTINGS
        // ----------------------

        SearchSettingsSection(
            selectedSide = selectedSide,
            onSideChange = {
                selectedSide = it
                // TODO: обновить кандидатов через DAO
            },
            rangeStart = rangeStart,
            onRangeChange = {
                rangeStart = it
                // TODO: обновить кандидатов через DAO
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------
        // CANDIDATES
        // ----------------------

        Text("Кандидаты", style = MaterialTheme.typography.titleMedium)

        CandidatesList(
            games = candidateGames,
            onAddClick = { game ->
                if (selectedGames.none { it.id == game.id }) {
                    selectedGames.add(
                        TemplateGameUiModel(
                            id = game.id,
                            name = game.name,
                            weight = 1
                        )
                    )
                }

                // TODO: обновить список кандидатов (исключить добавленные)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------
        // SELECTED
        // ----------------------

        Text("Выбранные партии", style = MaterialTheme.typography.titleMedium)

        SelectedGamesList(
            games = selectedGames,
            onRemove = { game ->
                selectedGames.remove(game)

                // TODO: возможно вернуть в кандидаты
            },
            onWeightChange = { game, newWeight ->
                val index = selectedGames.indexOfFirst { it.id == game.id }
                if (index != -1) {
                    selectedGames[index] = game.copy(weight = newWeight)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // ----------------------
        // BUTTONS
        // ----------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    // TODO: сохранить шаблон в БД
                    // данные:
                    // templateName
                    // selectedSide
                    // rangeStart
                    // selectedGames
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }

            OutlinedButton(
                onClick = {
                    // TODO: клонировать шаблон
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Клонировать")
            }
        }
    }
}