package com.ikhwan.mandarinkids.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScenarioProgressEntity::class, MasteredWordEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressDao(): ProgressDao
    abstract fun masteredWordDao(): MasteredWordDao

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

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milton_progress.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
