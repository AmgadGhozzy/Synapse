package io.synapse.ai.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /**
     * Migration from version 3 to 4.
     * Adds `updatedAt` and `isDirty` flags to `packs`, `questions`,
     * `question_progress`, and `study_sessions` for the new sync system.
     *
     * `updatedAt` is initialized to the current time to prevent everything from
     * appearing infinitely old and immediately triggering conflict overwrites.
     * `isDirty` defaults to 0 since existing data is assumed clean until edited.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val currentTime = System.currentTimeMillis()

            // 1. Migrate packs table
            db.execSQL("ALTER TABLE packs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE packs ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE packs SET updatedAt = $currentTime")

            // 2. Migrate questions table
            db.execSQL("ALTER TABLE questions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE questions ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE questions SET updatedAt = $currentTime")

            // 3. Migrate question_progress table
            db.execSQL("ALTER TABLE question_progress ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE question_progress SET updatedAt = $currentTime")

            // 4. Migrate study_sessions table
            db.execSQL("ALTER TABLE study_sessions ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE study_sessions SET updatedAt = $currentTime")
        }
    }

    /**
     * Migration from version 4 to 5.
     * Adds performance-critical indexes that were missing, causing full table
     * scans on every dashboard load:
     *  - questions(packId, isDeleted): covers GROUP BY / WHERE filter in stats aggregation
     *  - question_progress(lastReviewed): covers streak/reviewed-day lookups
     *  - study_sessions(finishedAt): covers daily-activity query filter
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_questions_packId_isDeleted ON questions (packId, isDeleted)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_question_progress_lastReviewed ON question_progress (lastReviewed)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_study_sessions_finishedAt ON study_sessions (finishedAt)")
        }
    }

    /**
     * Migration from version 5 to 6.
     * Adds the new `summaries` and `summary_sections` tables.
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `summaries` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` TEXT DEFAULT NULL,
                    `userId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `emoji` TEXT NOT NULL DEFAULT '📝',
                    `colorHex` TEXT NOT NULL DEFAULT '#10B981',
                    `shortSummary` TEXT DEFAULT NULL,
                    `language` TEXT NOT NULL DEFAULT 'en',
                    `difficulty` TEXT DEFAULT NULL,
                    `sourceType` TEXT DEFAULT NULL,
                    `sourceUrl` TEXT DEFAULT NULL,
                    `sourceHash` TEXT DEFAULT NULL,
                    `sectionCount` INTEGER NOT NULL DEFAULT 0,
                    `tags` TEXT NOT NULL DEFAULT '[]',
                    `learningObjectives` TEXT NOT NULL DEFAULT '[]',
                    `keyTakeaways` TEXT NOT NULL DEFAULT '[]',
                    `glossary` TEXT NOT NULL DEFAULT '[]',
                    `estimatedReadingMinutes` INTEGER DEFAULT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_summaries_remoteId` ON `summaries` (`remoteId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_summaries_createdAt` ON `summaries` (`createdAt`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_summaries_language` ON `summaries` (`language`)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `summary_sections` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `remoteId` TEXT DEFAULT NULL,
                    `summaryId` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `sortOrder` INTEGER NOT NULL DEFAULT 0,
                    `keyTakeaways` TEXT NOT NULL DEFAULT '[]',
                    `diagram` TEXT DEFAULT NULL,
                    `createdAt` INTEGER NOT NULL,
                    FOREIGN KEY(`summaryId`) REFERENCES `summaries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_summary_sections_summaryId` ON `summary_sections` (`summaryId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_summary_sections_remoteId` ON `summary_sections` (`remoteId`)")
        }
    }
}
