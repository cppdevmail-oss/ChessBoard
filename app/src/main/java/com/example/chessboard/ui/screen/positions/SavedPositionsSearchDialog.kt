package com.example.chessboard.ui.screen.positions

/**
 * Search dialog and filter helpers for saved positions.
 *
 * Keep saved-position search UI and name matching here. Do not add database calls or navigation decisions.
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.chessboard.ui.SavedPositionsSearchNameFieldTestTag
import com.example.chessboard.ui.components.AppTextField
import com.example.chessboard.ui.components.CardMetaText
import com.example.chessboard.ui.components.PrimaryButton
import com.example.chessboard.ui.components.SectionTitleText
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.Background
import com.example.chessboard.ui.theme.TextColor

internal data class SavedPositionsFilterState(
    val query: String = "",
    val isCaseSensitive: Boolean = false,
)

internal fun matchesSavedPositionsFilter(
    position: SavedPositionListItem,
    filterState: SavedPositionsFilterState,
): Boolean {
    if (filterState.query.isBlank()) {
        return true
    }

    if (filterState.isCaseSensitive) {
        return position.name.contains(filterState.query)
    }

    return position.name.contains(filterState.query, ignoreCase = true)
}

@Composable
internal fun RenderSavedPositionsSearchDialog(
    visible: Boolean,
    filterState: SavedPositionsFilterState,
    onDismiss: () -> Unit,
    onFilterStateChange: (SavedPositionsFilterState) -> Unit,
    onApplyClick: () -> Unit,
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
            SectionTitleText(text = "Search Positions")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd)
            ) {
                AppTextField(
                    value = filterState.query,
                    onValueChange = ::updateQuery,
                    label = "Position name",
                    placeholder = "Enter part of the name",
                    inputTestTag = SavedPositionsSearchNameFieldTestTag,
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
