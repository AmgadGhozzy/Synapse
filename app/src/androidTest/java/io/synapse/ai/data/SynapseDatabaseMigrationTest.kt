package io.synapse.ai.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SynapseDatabaseMigrationTest {

    private val dbName = "migration-test.db"
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getDatabasePath(dbName).delete()
    }

    @After
    fun teardown() {
        context.getDatabasePath(dbName).delete()
    }

    @Test
    fun testMigration3To4() {
        // 1. Create a raw SQLite DB representing version 3
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create packs table simulating version 3 schema
                        db.execSQL(
                            """
                            CREATE TABLE packs (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                uuid TEXT DEFAULT 'NULL',
                                title TEXT NOT NULL,
                                sourceType TEXT NOT NULL,
                                createdAt INTEGER NOT NULL,
                                note TEXT NOT NULL DEFAULT '',
                                category TEXT DEFAULT 'NULL',
                                emoji TEXT DEFAULT 'NULL',
                                color TEXT DEFAULT 'NULL',
                                language TEXT NOT NULL DEFAULT 'en',
                                isDeleted INTEGER NOT NULL DEFAULT 0,
                                difficulty TEXT DEFAULT 'NULL',
                                sourceUrl TEXT DEFAULT 'NULL',
                                sourceSummary TEXT DEFAULT 'NULL',
                                sourceHash TEXT DEFAULT 'NULL',
                                questionCount INTEGER NOT NULL DEFAULT 0,
                                packType TEXT NOT NULL DEFAULT 'ai_generated',
                                modules TEXT DEFAULT 'NULL',
                                tags TEXT NOT NULL DEFAULT '[]',
                                estimatedMinutes INTEGER,
                                isPremium INTEGER NOT NULL DEFAULT 0,
                                version INTEGER NOT NULL DEFAULT 1,
                                templateId TEXT DEFAULT 'NULL'
                            )
                            """.trimIndent()
                        )

                        // Create questions table simulating version 3 schema
                        db.execSQL(
                            """
                            CREATE TABLE questions (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                remoteId TEXT DEFAULT 'NULL',
                                packId INTEGER NOT NULL,
                                type TEXT NOT NULL,
                                questionText TEXT NOT NULL,
                                contentJson TEXT NOT NULL,
                                createdAt INTEGER NOT NULL,
                                reference TEXT DEFAULT 'NULL',
                                isDeleted INTEGER NOT NULL DEFAULT 0,
                                moduleTitle TEXT DEFAULT 'NULL',
                                level TEXT DEFAULT 'NULL',
                                objective TEXT DEFAULT 'NULL'
                            )
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                }).build()
        )

        // Insert sample data in version 3
        helper.writableDatabase.use { db ->
            db.execSQL("INSERT INTO packs (title, sourceType, createdAt) VALUES ('Test Pack', 'user', 1000)")
            db.execSQL("INSERT INTO questions (packId, type, questionText, contentJson, createdAt) VALUES (1, 'mcq', 'Test Q', '{}', 1000)")
        }

        // 2. Open the database using Room and run the migration
        val roomDb = Room.databaseBuilder(context, SynapseDatabase::class.java, dbName)
            .addMigrations(DatabaseMigrations.MIGRATION_3_4)
            .build()

        // 3. Verify data is preserved and new columns are initialized
        val packsCursor = roomDb.query("SELECT * FROM packs", null)
        assertTrue("Pack should exist", packsCursor.moveToFirst())
        assertEquals("Test Pack", packsCursor.getString(packsCursor.getColumnIndexOrThrow("title")))

        // Verify new columns
        val updatedAtIdx = packsCursor.getColumnIndexOrThrow("updatedAt")
        val isDirtyIdx = packsCursor.getColumnIndexOrThrow("isDirty")

        val updatedAt = packsCursor.getLong(updatedAtIdx)
        val isDirty = packsCursor.getInt(isDirtyIdx)

        assertTrue("updatedAt should be > 0", updatedAt > 0)
        assertEquals(0, isDirty)
        packsCursor.close()

        roomDb.close()
    }
}
