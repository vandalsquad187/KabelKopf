package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY level DESC, currentXp DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE category = :category OR category = ''")
    fun getSkillsForCategory(category: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkillById(id: String): SkillEntity?

    @Query("SELECT * FROM skills WHERE category = :category OR category = ''")
    suspend fun getSkillsForCategoryOnce(category: String): List<SkillEntity>

    @Query("SELECT * FROM skills")
    suspend fun getAllSkillsOnce(): List<SkillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Query("DELETE FROM skills")
    suspend fun deleteAll()
}
