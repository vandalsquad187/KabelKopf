package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY tier DESC, isUnlocked ASC, name ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET isUnlocked = 0, unlockedAt = NULL WHERE id = :id")
    suspend fun lock(id: String)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
