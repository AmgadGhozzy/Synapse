package io.synapse.ai.domain.stats

object StreakCalculator {

    fun currentStreak(studiedDayIndices: List<Long>, todayDayIndex: Long): Int {
        if (studiedDayIndices.isEmpty()) return 0

        val startIndex = when {
            studiedDayIndices.first() == todayDayIndex     -> todayDayIndex
            studiedDayIndices.first() == todayDayIndex - 1 -> todayDayIndex - 1
            else                                            -> return 0
        }

        var streak = 0
        var expected = startIndex
        for (dayIndex in studiedDayIndices) {
            if (dayIndex == expected) {
                streak++
                expected--
            } else if (dayIndex < expected) {
                break
            }
        }
        return streak
    }

    fun bestStreak(studiedDayIndices: List<Long>): Int {
        if (studiedDayIndices.isEmpty()) return 0

        var best = 1
        var current = 1
        for (i in 1 until studiedDayIndices.size) {
            if (studiedDayIndices[i - 1] - studiedDayIndices[i] == 1L) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    fun weekDots(
        studiedDayIndices: List<Long>,
        todayDayIndex: Long,
        weekStartOffset: Int = 0,
    ): List<Boolean> {
        val todayDow = ((todayDayIndex % 7) + 3).toInt() % 7
        val mondayDayIndex = todayDayIndex - ((todayDow - weekStartOffset + 7) % 7)

        val studiedSet = studiedDayIndices.toHashSet()
        return List(7) { offset -> studiedSet.contains(mondayDayIndex + offset) }
    }

    fun currentWeekBounds(nowMs: Long): Pair<Long, Long> {
        val todayIndex = nowMs / 86_400_000L
        val todayDow  = ((todayIndex % 7) + 3).toInt() % 7
        val mondayMs  = (todayIndex - todayDow) * 86_400_000L
        return mondayMs to (mondayMs + 7 * 86_400_000L)
    }
}
