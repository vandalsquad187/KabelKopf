package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.SkillDao
import com.docai.kabelkopf.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class SkillProgress(
    val name: String,
    val currentXp: Int,
    val nextLevelXp: Int,
    val level: Int,
    val maxLevel: Int,
    val progressPercent: Float
)

@Singleton
class SkillRepository @Inject constructor(
    private val skillDao: SkillDao
) {
    fun getAllSkills(): Flow<List<SkillEntity>> = skillDao.getAllSkills()

    suspend fun getAllSkillsOnce(): List<SkillEntity> = skillDao.getAllSkillsOnce()

    suspend fun getSkillById(id: String): SkillEntity? = skillDao.getSkillById(id)

    suspend fun updateSkill(skill: SkillEntity) = skillDao.updateSkill(skill)

    suspend fun addXp(skillId: String, xp: Int) {
        val skill = skillDao.getSkillById(skillId) ?: return
        var remainingXp = skill.currentXp + xp
        var newLevel = skill.level
        while (remainingXp >= skill.nextLevelXp && newLevel < skill.maxLevel) {
            remainingXp -= skill.nextLevelXp
            newLevel++
        }
        skillDao.updateSkill(skill.copy(
            currentXp = remainingXp.coerceAtLeast(0),
            level = newLevel,
            nextLevelXp = xpForNextLevel(newLevel)
        ))
    }

    private fun xpForNextLevel(level: Int): Int = level * 100

    suspend fun getSkillsForCategoryOnce(category: String): List<SkillEntity> =
        skillDao.getSkillsForCategoryOnce(category)

    suspend fun getAllSkillProgress(): List<SkillProgress> {
        val skills = skillDao.getAllSkillsOnce()
        return skills.map { s ->
            val percent = if (s.nextLevelXp > 0)
                (s.currentXp.toFloat() / s.nextLevelXp * 100).coerceAtMost(100f)
            else 100f
            SkillProgress(
                name = s.name,
                currentXp = s.currentXp,
                nextLevelXp = s.nextLevelXp,
                level = s.level,
                maxLevel = s.maxLevel,
                progressPercent = percent
            )
        }
    }

    suspend fun getTotalXp(): Int {
        val skills = skillDao.getAllSkillsOnce()
        return skills.sumOf { it.currentXp }
    }

    suspend fun getOverallProgressPercent(): Float {
        val skills = skillDao.getAllSkillsOnce()
        if (skills.isEmpty()) return 0f
        val total = skills.sumOf {
            if (it.nextLevelXp > 0) it.nextLevelXp else it.currentXp
        }
        val current = skills.sumOf { it.currentXp }
        return if (total > 0) (current.toFloat() / total * 100).coerceAtMost(100f) else 0f
    }
}
