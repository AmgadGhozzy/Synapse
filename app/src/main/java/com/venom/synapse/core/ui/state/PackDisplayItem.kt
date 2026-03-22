package com.venom.synapse.core.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class PackDisplayItem(
    val id: Long,
    val title: String,
    val category: String,
    val emoji: String,
    val colorHex: String,
    val totalCards: Int,
    val cardsToReview: Int,
    val masteredCards: Int,
    val progress: Float,
    val lastStudiedLabel: LastStudiedLabel,
    val streakDays: Int
) {
    companion object {
        val Mocks = listOf(
            PackDisplayItem(
                id = 1L,
                title = "Machine Learning Basics",
                category = "Science",
                emoji = "🤖",
                colorHex = "#4F46E5",
                totalCards = 120,
                cardsToReview = 14,
                masteredCards = 90,
                progress = 0.75f,
                lastStudiedLabel = LastStudiedLabel.Today,
                streakDays = 7
            ),
            PackDisplayItem(
                id = 2L,
                title = "History of Ancient Rome",
                category = "History",
                emoji = "🏛️",
                colorHex = "#10B981",
                totalCards = 86,
                cardsToReview = 22,
                masteredCards = 39,
                progress = 0.45f,
                lastStudiedLabel = LastStudiedLabel.Yesterday,
                streakDays = 3
            ),
            PackDisplayItem(
                id = 3L,
                title = "Organic Chemistry",
                category = "Science",
                emoji = "⚗️",
                colorHex = "#F59E0B",
                totalCards = 200,
                cardsToReview = 48,
                masteredCards = 40,
                progress = 0.20f,
                lastStudiedLabel = LastStudiedLabel.DaysAgo(3),
                streakDays = 0
            )
        )
    }
}