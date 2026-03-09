package com.venom.synapse.domain.model

data class SessionWithQuestionsModel(
    val session: StudySessionModel,
    val questions: List<QuestionModel>
)

data class StudySessionModel(
    val id: Long = 0L,
    val packId: Long,
    val mode: SessionMode,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val summaryJson: String? = null,
    val snapshotQuestions: Boolean = false
)
