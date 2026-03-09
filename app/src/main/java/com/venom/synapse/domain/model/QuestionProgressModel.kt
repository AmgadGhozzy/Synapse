package com.venom.synapse.domain.model

/**
 * SRS progress fields for a single question.
 * Math logic for nextReview should be handled in StudyEngine (Layer 3).
 */
data class QuestionProgressModel(
    val questionId: Long,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReview: Long? = null,
    val lastReviewed: Long? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)
