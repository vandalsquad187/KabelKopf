package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.AchievementDao
import com.docai.kabelkopf.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    fun getAllAchievements(): Flow<List<AchievementEntity>> =
        achievementDao.getAllAchievements()

    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> =
        achievementDao.getUnlockedAchievements()

    suspend fun getUnlockedCount(): Int = achievementDao.getUnlockedCount()

    suspend fun getTotalCount(): Int = achievementDao.getTotalCount()

    suspend fun getAchievementById(id: String): AchievementEntity? =
        achievementDao.getAchievementById(id)

    suspend fun unlock(id: String) = achievementDao.unlock(id)

    suspend fun isUnlocked(id: String): Boolean {
        return achievementDao.getAchievementById(id)?.isUnlocked == true
    }
}
