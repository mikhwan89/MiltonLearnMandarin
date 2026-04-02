package com.ikhwan.mandarinkids.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScenarioProgressEntity::class, MasteredWordEntity::class, MilestoneReward::class, CustomScenarioEntity::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressDao(): ProgressDao
    abstract fun masteredWordDao(): MasteredWordDao
    abstract fun milestoneRewardDao(): MilestoneRewardDao
    abstract fun customScenarioDao(): CustomScenarioDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mastered_words (
                        scenarioId TEXT NOT NULL,
                        chinese    TEXT NOT NULL,
                        pinyin     TEXT NOT NULL,
                        english    TEXT NOT NULL,
                        indonesian TEXT NOT NULL,
                        note       TEXT,
                        masteredAt INTEGER NOT NULL,
                        PRIMARY KEY (scenarioId, chinese)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mastered_words ADD COLUMN boxLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE mastered_words ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS milestone_rewards (
                        id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        milestoneType TEXT NOT NULL,
                        targetValue   INTEGER NOT NULL,
                        rewardText    TEXT NOT NULL,
                        isClaimed     INTEGER NOT NULL DEFAULT 0,
                        createdAt     INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scenario_progress ADD COLUMN speechRateOverride REAL")
            }
        }

        // Purge mastered_words rows whose Chinese text has 5+ characters — these are
        // stale pre-split entries from before the 4-character-max flashcard refactor.
        // SQLite has no Unicode-aware length for CJK, so we delete rows where the byte
        // length (each CJK char = 3 UTF-8 bytes) is ≥ 15, i.e. 5 × 3 = 15 bytes.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM mastered_words WHERE length(chinese) >= 15")
            }
        }

        // Add practiceType to the primary key so each word has separate progress
        // per practice modality (Default / Listening / Reading).
        // Existing rows are preserved as practiceType = 'DEFAULT'.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mastered_words_new (
                        scenarioId    TEXT NOT NULL,
                        chinese       TEXT NOT NULL,
                        practiceType  TEXT NOT NULL,
                        pinyin        TEXT NOT NULL,
                        english       TEXT NOT NULL,
                        indonesian    TEXT NOT NULL,
                        note          TEXT,
                        masteredAt    INTEGER NOT NULL,
                        boxLevel      INTEGER NOT NULL DEFAULT 1,
                        nextReviewDate INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (scenarioId, chinese, practiceType)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO mastered_words_new
                    SELECT scenarioId, chinese, 'DEFAULT', pinyin, english, indonesian,
                           note, masteredAt, boxLevel, nextReviewDate
                    FROM mastered_words
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE mastered_words")
                db.execSQL("ALTER TABLE mastered_words_new RENAME TO mastered_words")
            }
        }

        // Migrate milestone_rewards: replace milestoneType+targetValue with
        // conditionsJson (JSON array) + logic (AND/OR) to support multi-condition rewards.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS milestone_rewards_new (
                        id             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conditionsJson TEXT NOT NULL,
                        logic          TEXT NOT NULL DEFAULT 'AND',
                        rewardText     TEXT NOT NULL,
                        isClaimed      INTEGER NOT NULL DEFAULT 0,
                        createdAt      INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // Wrap each existing single condition in a JSON array
                db.execSQL(
                    """
                    INSERT INTO milestone_rewards_new
                        (id, conditionsJson, logic, rewardText, isClaimed, createdAt)
                    SELECT id,
                           '[{"type":"' || milestoneType || '","targetValue":' || targetValue || '}]',
                           'AND',
                           rewardText, isClaimed, createdAt
                    FROM milestone_rewards
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE milestone_rewards")
                db.execSQL("ALTER TABLE milestone_rewards_new RENAME TO milestone_rewards")
            }
        }

        // Add masteryLevel column to scenario_progress (default 1 = first quiz).
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scenario_progress ADD COLUMN masteryLevel INTEGER NOT NULL DEFAULT 1")
            }
        }

        // Add starsAtCurrentLevel — tracks stars for the active level; resets to 0 on level-up.
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scenario_progress ADD COLUMN starsAtCurrentLevel INTEGER NOT NULL DEFAULT 0")
                // Seed existing rows: if they have stars, those were earned at level 1
                db.execSQL("UPDATE scenario_progress SET starsAtCurrentLevel = stars WHERE masteryLevel = 1")
            }
        }

        // Add custom_scenarios table to store parent-created scenario JSON blobs.
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_scenarios (
                        id TEXT NOT NULL PRIMARY KEY,
                        scenarioJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milton_progress.db"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
                        MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11
                    )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
