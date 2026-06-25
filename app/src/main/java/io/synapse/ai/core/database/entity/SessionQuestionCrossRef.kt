package io.synapse.ai.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "session_question_cross_ref",
    primaryKeys = ["sessionId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = StudySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId", "questionId"]), Index(value = ["questionId"])]
)
data class SessionQuestionCrossRef(
    val sessionId: Long,
    val questionId: Long
)

data class SessionWithQuestions(
    val session: StudySessionEntity,
    val questions: List<QuestionEntity>
)
