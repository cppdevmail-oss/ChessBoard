package com.example.chessboard.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessboard.ui.components.AppBottomNavigation
import com.example.chessboard.ui.components.AppDivider
import com.example.chessboard.ui.components.AppScreenScaffold
import com.example.chessboard.ui.components.AppSearchField
import com.example.chessboard.ui.components.defaultAppBottomNavigationItems
import com.example.chessboard.ui.theme.AppDimens
import com.example.chessboard.ui.theme.TextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.chessboard.ui.theme.TrainingAccentTeal
import com.example.chessboard.ui.theme.TrainingErrorRed
import com.example.chessboard.ui.theme.TrainingSuccessGreen
import com.example.chessboard.ui.theme.TrainingWarningOrange

private val SmartTrainingCardBg = Color(0xFF0D2318)
private val SmartTrainingIconBg = Color(0xFF179A6F)
private val SmartTrainingItemBg = Color(0xFF141414)
private val SmartTrainingItemBorder = Color(0xFF222222)
private val BeginnerBadgeBg = Color(0xFF1A3D2A)
private val IntermediateBadgeBg = Color(0xFF3D2A0A)

enum class OpeningDifficulty { BEGINNER, INTERMEDIATE, ADVANCED }

private data class SmartOpeningItem(
    val name: String,
    val eco: String,
    val lastPracticed: String?,
    val difficulty: OpeningDifficulty,
)

private val dummyOpenings = listOf(
    SmartOpeningItem("Italian Game", "C50", null, OpeningDifficulty.BEGINNER),
    SmartOpeningItem("Sicilian Defense", "B20", null, OpeningDifficulty.INTERMEDIATE),
    SmartOpeningItem("Queen's Gambit", "D06", "2 days ago", OpeningDifficulty.INTERMEDIATE),
    SmartOpeningItem("Ruy Lopez", "C60", "5 days ago", OpeningDifficulty.ADVANCED),
    SmartOpeningItem("King's Indian Defense", "E60", null, OpeningDifficulty.ADVANCED),
    SmartOpeningItem("French Defense", "C00", "1 week ago", OpeningDifficulty.BEGINNER),
)

@Composable
fun SmartTrainingScreenContainer(
    screenContext: ScreenContainerContext,
    modifier: Modifier = Modifier,
) {
    val userProfileService = remember(screenContext.inDbProvider) {
        screenContext.inDbProvider.createUserProfileService()
    }
    var infoCardHidden by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val profile = withContext(Dispatchers.IO) { userProfileService.getProfile() }
        infoCardHidden = profile.hideSmartTrainingInfoCard
    }

    SmartTrainingScreen(
        infoCardHidden = infoCardHidden,
        onDismissInfoCard = {
            infoCardHidden = true
            scope.launch(Dispatchers.IO) { userProfileService.setHideSmartTrainingInfoCard(true) }
        },
        onNavigate = screenContext.onNavigate,
        modifier = modifier,
    )
}

@Composable
fun SmartTrainingScreen(
    infoCardHidden: Boolean = false,
    onDismissInfoCard: () -> Unit = {},
    onNavigate: (ScreenType) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(searchQuery) {
        dummyOpenings.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        }
    }
    val selectedCount = selectedIds.value.size

    AppScreenScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SmartTrainingTopBar()
        },
        bottomBar = {
            AppBottomNavigation(
                items = defaultAppBottomNavigationItems(),
                selectedItem = ScreenType.SmartTraining,
                onItemSelected = onNavigate,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = AppDimens.spaceLg,
                vertical = AppDimens.spaceLg,
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spaceLg),
        ) {
            if (!infoCardHidden) {
                item {
                    HowItWorksCard(onDismiss = onDismissInfoCard)
                }
            }

            item {
                SelectOpeningsHeader(
                    selectedCount = selectedCount,
                    onSelectAll = {
                        selectedIds.value = dummyOpenings.map { it.eco }.toSet()
                    },
                    onClear = { selectedIds.value = emptySet() },
                )
            }

            item {
                AppSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search openings...",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            items(filtered.size) { index ->
                val item = filtered[index]
                val isChecked = item.eco in selectedIds.value
                OpeningSelectRow(
                    item = item,
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        selectedIds.value = if (checked) {
                            selectedIds.value + item.eco
                        } else {
                            selectedIds.value - item.eco
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SmartTrainingTopBar(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.spaceLg, vertical = AppDimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SmartTrainingIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.spaceLg))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Smart Training",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextColor.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = TrainingAccentTeal,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = "Select openings to practice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextColor.Secondary,
                )
            }
        }
        AppDivider()
    }
}

@Composable
private fun HowItWorksCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusLg))
            .background(SmartTrainingCardBg)
            .padding(AppDimens.spaceLg),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spaceMd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SmartTrainingIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.spaceMd))
            Text(
                text = "How Your Training Works",
                style = MaterialTheme.typography.titleMedium,
                color = TextColor.Primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = TextColor.Secondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onDismiss),
            )
        }

        Text(
            text = "We use a smart repetition system to help you remember openings more effectively:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor.Secondary,
        )

        HowItWorksRow(
            icon = Icons.Filled.Close,
            iconTint = TrainingErrorRed,
            boldPart = "Mistakes",
            rest = " — repeated every day until you fix them",
        )
        HowItWorksRow(
            icon = Icons.Filled.Warning,
            iconTint = TrainingWarningOrange,
            boldPart = "Uncertain moves",
            rest = " — reviewed every 2–3 days to build confidence",
        )
        HowItWorksRow(
            icon = Icons.Filled.CheckBox,
            iconTint = TrainingSuccessGreen,
            boldPart = "Known positions",
            rest = " — revisited every 5–10 days to keep them fresh",
        )

        Spacer(modifier = Modifier.height(AppDimens.spaceXs))

        Text(
            text = "This way, you focus more on what you struggle with, while still reinforcing what you already know — so nothing gets forgotten.",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = TextColor.Secondary,
        )
    }
}

@Composable
private fun HowItWorksRow(
    icon: ImageVector,
    iconTint: Color,
    boldPart: String,
    rest: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp),
        )
        Spacer(modifier = Modifier.width(AppDimens.spaceMd))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextColor.Primary)) {
                    append(boldPart)
                }
                withStyle(SpanStyle(color = TextColor.Primary)) {
                    append(rest)
                }
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SelectOpeningsHeader(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Select Openings",
                style = MaterialTheme.typography.headlineSmall,
                color = TextColor.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$selectedCount openings selected",
                style = MaterialTheme.typography.bodySmall,
                color = TextColor.Secondary,
            )
        }
        Text(
            text = "Select All",
            style = MaterialTheme.typography.bodyMedium,
            color = TrainingAccentTeal,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimens.radiusSm))
                .background(Color.Transparent)
                .padding(horizontal = AppDimens.spaceSm, vertical = AppDimens.spaceXs),
        )
        Spacer(modifier = Modifier.width(AppDimens.spaceSm))
        Text(
            text = "Clear",
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor.Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(AppDimens.radiusSm))
                .background(Color.Transparent)
                .padding(horizontal = AppDimens.spaceSm, vertical = AppDimens.spaceXs),
        )
    }
}

@Composable
private fun OpeningSelectRow(
    item: SmartOpeningItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusMd))
            .background(SmartTrainingItemBg)
            .border(1.dp, SmartTrainingItemBorder, RoundedCornerShape(AppDimens.radiusMd))
            .padding(horizontal = AppDimens.spaceMd, vertical = AppDimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = TrainingAccentTeal,
                uncheckedColor = TextColor.Secondary,
                checkmarkColor = Color.White,
            ),
        )
        Spacer(modifier = Modifier.width(AppDimens.spaceSm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextColor.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.eco,
                style = MaterialTheme.typography.bodySmall,
                color = TextColor.Secondary,
            )
            Text(
                text = item.lastPracticed?.let { "Last practiced $it" } ?: "Not practiced yet",
                style = MaterialTheme.typography.bodySmall,
                color = TextColor.Secondary,
            )
        }
        DifficultyBadge(difficulty = item.difficulty)
    }
}

@Composable
private fun DifficultyBadge(
    difficulty: OpeningDifficulty,
    modifier: Modifier = Modifier,
) {
    val (label, textColor, bgColor) = when (difficulty) {
        OpeningDifficulty.BEGINNER -> Triple("beginner", TrainingAccentTeal, BeginnerBadgeBg)
        OpeningDifficulty.INTERMEDIATE -> Triple("intermediate", TrainingWarningOrange, IntermediateBadgeBg)
        OpeningDifficulty.ADVANCED -> Triple("advanced", TrainingErrorRed, Color(0xFF3D0A0A))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.radiusPill))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
    }
}
