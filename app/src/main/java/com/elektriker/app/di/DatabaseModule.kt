package com.elektriker.app.di

import android.content.Context
import androidx.room.Room
import com.elektriker.app.data.local.AppDatabase
import com.elektriker.app.data.local.dao.*
import com.elektriker.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DB_NAME
        ).addCallback(AppDatabase.seederCallback)
            .addMigrations(AppDatabase.MIGRATION_6_7)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides fun provideWorkTaskDao(db: AppDatabase): WorkTaskDao = db.workTaskDao()
    @Provides fun provideWorkStepDao(db: AppDatabase): WorkStepDao = db.workStepDao()
    @Provides fun provideErrorLogDao(db: AppDatabase): ErrorLogDao = db.errorLogDao()
    @Provides fun provideKnowledgeBaseDao(db: AppDatabase): KnowledgeBaseDao = db.knowledgeBaseDao()
    @Provides fun provideWorkflowTemplateDao(db: AppDatabase): WorkflowTemplateDao = db.workflowTemplateDao()
    @Provides fun providePhotoDao(db: AppDatabase): PhotoDao = db.photoDao()
    @Provides fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
    @Provides fun provideErrorCauseDao(db: AppDatabase): ErrorCauseDao = db.errorCauseDao()
    @Provides fun provideSkillDao(db: AppDatabase): SkillDao = db.skillDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideChecklistDao(db: AppDatabase): ChecklistDao = db.checklistDao()
    @Provides fun provideProjectAssetDao(db: AppDatabase): ProjectAssetDao = db.projectAssetDao()
}
