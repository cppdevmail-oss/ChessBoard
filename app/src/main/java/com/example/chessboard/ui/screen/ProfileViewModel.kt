package com.example.chessboard.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileState(
    val userName: String = "Chess Enthusiast",
    val level: Int = 1,
    val totalMoves: Int = 0,
    val levelMoveThreshold: Int = 50,
    val accuracy: Int = 0,
    val bestStreak: Int = 0,
    val achievements: List<AchievementItem> = defaultAchievements()
)

data class AchievementItem(
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false
)

private fun defaultAchievements() = listOf(
    AchievementItem("First Steps", "Complete your first training session"),
    AchievementItem("Dedication", "Practice 100 moves"),
    AchievementItem("Opening Master", "Learn 10 different openings"),
    AchievementItem("Streak King", "Achieve a 5-move perfect streak"),
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
}
