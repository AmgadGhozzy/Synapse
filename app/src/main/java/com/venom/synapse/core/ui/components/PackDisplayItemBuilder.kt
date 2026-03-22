package com.venom.synapse.core.ui.components

import com.venom.synapse.core.ui.state.LastStudiedLabel
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.domain.model.PackModel
import com.venom.synapse.domain.model.QuestionProgressModel
import com.venom.synapse.domain.repo.IProgressRepository
import com.venom.synapse.domain.repo.IQuestionRepository

object PackDisplayItemBuilder {
    suspend fun build(
        pack: PackModel,
        questionRepo: IQuestionRepository,
        progressRepo: IProgressRepository,
    ): PackDisplayItem {
        val totalCards   = questionRepo.countByPack(pack.id)
        val dueQuestions = questionRepo.getDueQuestions(pack.id, limit = Int.MAX_VALUE)
        val dueCards     = dueQuestions.size

        val progressRecords: List<QuestionProgressModel> =
            dueQuestions.mapNotNull { progressRepo.getProgress(it.id) }

        val masteredInDue = progressRecords.count { p ->
            p.repetitions >= MASTERY_REPETITIONS && p.intervalDays >= MASTERY_INTERVAL_DAYS
        }
        val notDueCards   = (totalCards - dueCards).coerceAtLeast(0)
        val masteredCards = (masteredInDue + notDueCards).coerceAtMost(totalCards)

        val lastReviewed = progressRepo.getLastReviewedForPack(pack.id)
        val streakDays   = computeStreakDays(progressRecords)

        return PackDisplayItem(
            id = pack.id,
            title = pack.title,
            category = pack.category.orEmpty().ifBlank { DEFAULT_CATEGORY },
            emoji = pack.emoji.orEmpty().ifBlank { DEFAULT_EMOJI },
            colorHex = pack.color.orEmpty().ifBlank { DEFAULT_COLOR_HEX },
            totalCards = totalCards,
            cardsToReview = dueCards,
            masteredCards = masteredCards,
            progress = if (totalCards > 0) masteredCards / totalCards.toFloat() else 0f,
            lastStudiedLabel = LastStudiedLabel.Companion.from(lastReviewed),
            streakDays = streakDays,
        )
    }

    private fun computeStreakDays(records: List<QuestionProgressModel>): Int {
        val reviewedEpochDays = records
            .mapNotNull { it.lastReviewed }
            .map { ms -> ms / MS_PER_DAY }
            .toHashSet()

        if (reviewedEpochDays.isEmpty()) return 0

        val todayEpochDay = System.currentTimeMillis() / MS_PER_DAY
        var anchor = todayEpochDay

        if (!reviewedEpochDays.contains(anchor)) {
            anchor--
            if (!reviewedEpochDays.contains(anchor)) return 0
        }

        var streak = 0
        while (reviewedEpochDays.contains(anchor)) {
            streak++
            anchor--
        }
        return streak
    }

    private const val MASTERY_REPETITIONS   = 3
    private const val MASTERY_INTERVAL_DAYS = 7
    private const val MS_PER_DAY            = 86_400_000L
    private const val DEFAULT_EMOJI         = "📚"
    private const val DEFAULT_COLOR_HEX     = "#4F46E5"
    private const val DEFAULT_CATEGORY      = "General"
}