package com.venom.synapse.domain.model

data class DayActivityModel(
    /** UTC midnight epoch-ms. */
    val dayEpochMs: Long,
    val questionsStudied: Int,
    val correctCount: Int,
    val sessionCount: Int,
    val totalDurationMs: Long,
) {
    /** 0..1 — safe against zero questionsStudied. */
    val accuracy: Float
        get() = if (questionsStudied == 0) 0f
                else correctCount.toFloat() / questionsStudied
}
