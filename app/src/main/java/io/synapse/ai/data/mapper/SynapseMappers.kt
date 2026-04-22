package io.synapse.ai.data.mapper

import io.synapse.ai.data.entity.PackEntity
import io.synapse.ai.data.entity.QuestionEntity
import io.synapse.ai.data.entity.QuestionProgressEntity
import io.synapse.ai.data.entity.StudySessionEntity
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.model.QuestionModel
import io.synapse.ai.domain.model.QuestionProgressModel
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.domain.model.SessionMode
import io.synapse.ai.domain.model.SourceType
import io.synapse.ai.domain.model.StudySessionModel

// PACK
fun PackEntity.toDomain(): PackModel = PackModel(
    id         = id,
    uuid       = uuid,
    title      = title,
    sourceType = SourceType.fromString(sourceType),
    createdAt  = createdAt,
    note       = note,
    category   = category,
    emoji      = emoji,
    color      = color,
    language   = language,
)

fun PackModel.toEntity(): PackEntity = PackEntity(
    id         = id,
    uuid       = uuid,
    title      = title,
    sourceType = sourceType.name,
    createdAt  = createdAt,
    note       = note,
    category   = category,
    emoji      = emoji,
    color      = color,
    language   = language,
)

// QUESTION
// Parses contentJson via SynapseContentAdapter using type as discriminator.
fun QuestionEntity.toDomain(): QuestionModel {
    val questionType = QuestionType.fromString(type)
    return QuestionModel(
        id           = id,
        packId       = packId,
        type         = questionType,
        questionText = questionText,
        content      = SynapseContentAdapter.fromJson(questionType, contentJson),
        createdAt    = createdAt,
        remoteId     = remoteId,
        reference    = reference,
        sourcePage   = sourcePage,
    )
}

fun QuestionModel.toEntity(): QuestionEntity = QuestionEntity(
    id           = id,
    packId       = packId,
    type         = type.name,
    questionText = questionText,
    contentJson  = SynapseContentAdapter.toJson(content),
    createdAt    = createdAt,
    remoteId     = remoteId,
    reference    = reference,
    sourcePage   = sourcePage,
)

// QUESTION PROGRESS
fun QuestionProgressEntity.toDomain(): QuestionProgressModel = QuestionProgressModel(
    questionId   = questionId,
    easeFactor   = easeFactor,
    intervalDays = intervalDays,
    repetitions  = repetitions,
    nextReview   = nextReview,
    lastReviewed = lastReviewed,
    correctCount = correctCount,
    wrongCount   = wrongCount,
)

fun QuestionProgressModel.toEntity(): QuestionProgressEntity = QuestionProgressEntity(
    questionId   = questionId,
    easeFactor   = easeFactor,
    intervalDays = intervalDays,
    repetitions  = repetitions,
    nextReview   = nextReview,
    lastReviewed = lastReviewed,
    correctCount = correctCount,
    wrongCount   = wrongCount,
    updatedAt    = System.currentTimeMillis(),
)

// STUDY SESSION
fun StudySessionEntity.toDomain(): StudySessionModel = StudySessionModel(
    id                = id,
    packId            = packId,
    mode              = SessionMode.fromString(mode),
    startedAt         = startedAt,
    finishedAt        = finishedAt,
    summaryJson       = summaryJson,
    snapshotQuestions = snapshotQuestions,
)

fun StudySessionModel.toEntity(): StudySessionEntity = StudySessionEntity(
    id                = id,
    packId            = packId,
    mode              = mode.name,
    startedAt         = startedAt,
    finishedAt        = finishedAt,
    summaryJson       = summaryJson,
    snapshotQuestions = snapshotQuestions,
    updatedAt         = System.currentTimeMillis(),
)