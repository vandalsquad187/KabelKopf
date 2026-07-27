package com.elektriker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elektriker.app.data.local.callback.DatabaseSeeder
import com.elektriker.app.data.local.dao.*
import com.elektriker.app.data.local.entity.*

@Database(
    entities = [
        WorkTaskEntity::class,
        WorkStepEntity::class,
        ErrorLogEntity::class,
        KnowledgeBaseEntity::class,
        WorkflowTemplateEntity::class,
        PhotoEntity::class,
        ProjectEntity::class,
        ErrorCauseEntity::class,
        SkillEntity::class,
        AchievementEntity::class,
        ChecklistEntity::class,
        ChecklistItemEntity::class,
        ProjectAssetEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workTaskDao(): WorkTaskDao
    abstract fun workStepDao(): WorkStepDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun knowledgeBaseDao(): KnowledgeBaseDao
    abstract fun workflowTemplateDao(): WorkflowTemplateDao
    abstract fun photoDao(): PhotoDao
    abstract fun projectDao(): ProjectDao
    abstract fun errorCauseDao(): ErrorCauseDao
    abstract fun skillDao(): SkillDao
    abstract fun achievementDao(): AchievementDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun projectAssetDao(): ProjectAssetDao

    companion object {
        val seederCallback = DatabaseSeeder()

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS project_assets (
                        id TEXT PRIMARY KEY NOT NULL,
                        projectId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        location TEXT NOT NULL DEFAULT '',
                        fuse_type TEXT NOT NULL DEFAULT '',
                        circuit_info TEXT NOT NULL DEFAULT '',
                        uv_type TEXT NOT NULL DEFAULT '',
                        description TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        photo_paths TEXT NOT NULL DEFAULT '',
                        doc_paths TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}
