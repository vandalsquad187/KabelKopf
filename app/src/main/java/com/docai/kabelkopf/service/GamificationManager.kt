package com.docai.kabelkopf.service

import com.docai.kabelkopf.data.local.dao.WorkStepDao
import com.docai.kabelkopf.data.local.dao.WorkTaskDao
import com.docai.kabelkopf.data.local.dao.ErrorLogDao
import com.docai.kabelkopf.data.local.dao.ProjectDao
import com.docai.kabelkopf.data.local.dao.SkillDao
import com.docai.kabelkopf.data.repository.AchievementRepository
import com.docai.kabelkopf.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationManager @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val workTaskDao: WorkTaskDao,
    private val errorLogDao: ErrorLogDao,
    private val skillDao: SkillDao,
    private val projectDao: ProjectDao,
    private val workStepDao: WorkStepDao
) {
    suspend fun checkOnTaskCreated(taskCategory: String) {
        checkAndUnlock("first_task") {
            workTaskDao.getTaskCountOnce() >= 1
        }
        checkAndUnlock("five_tasks") {
            workTaskDao.getTaskCountOnce() >= 5
        }
        checkAndUnlock("hundred_tasks") {
            workTaskDao.getTaskCountOnce() >= 100
        }
        checkAndUnlock("all_categories") {
            val usedCategories = workTaskDao.getAllUsedCategoriesOnce()
            usedCategories.containsAll(Constants.Categories.all)
        }
    }

    suspend fun checkOnTaskCompleted(taskId: String) {
        val errorCount = errorLogDao.getErrorCountByTask(taskId)
        checkAndUnlock("error_free") {
            errorCount == 0
        }
    }

    suspend fun checkOnErrorLogged(errorsWithSolutionCount: Int) {
        checkAndUnlock("first_error") {
            true
        }
        checkAndUnlock("solutions_5") {
            errorsWithSolutionCount >= 5
        }
    }

    suspend fun checkOnRatingGiven(rating: Int) {
        checkAndUnlock("rating_5") {
            rating == 5
        }
    }

    suspend fun checkOnSkillChange() {
        val allSkills = skillDao.getAllSkillsOnce()
        checkAndUnlock("skill_level_5") {
            allSkills.any { it.level >= 5 }
        }
        checkAndUnlock("skill_max") {
            allSkills.any { it.level >= it.maxLevel }
        }
        checkAndUnlock("all_max_levels") {
            allSkills.isNotEmpty() && allSkills.all { it.level >= it.maxLevel }
        }
    }

    suspend fun checkOnProjectCreated() {
        checkAndUnlock("ten_projects") {
            projectDao.getProjectCount() >= 10
        }
    }

    suspend fun checkOnStreak() {
        checkAndUnlock("streak_7") {
            getCurrentStreak() >= 7
        }
        checkAndUnlock("streak_30") {
            getCurrentStreak() >= 30
        }
    }

    suspend fun checkMasterElectrician() {
        val total = achievementRepository.getTotalCount()
        val unlocked = achievementRepository.getUnlockedCount()
        checkAndUnlock("master_electrician") {
            total > 0 && unlocked >= total - 1
        }
    }

    private suspend fun getCurrentStreak(): Int {
        val dates = workTaskDao.getAllTaskDates()
        if (dates.isEmpty()) return 0
        val sorted = dates.sortedDescending()
        var streak = 0
        val today = java.time.LocalDate.now()
        for (i in sorted.indices) {
            val date = java.time.Instant.ofEpochMilli(sorted[i])
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val expected = today.minusDays(streak.toLong())
            if (date == expected || date == expected.minusDays(1)) {
                if (date == expected) streak++
            } else {
                break
            }
        }
        return streak
    }

    private suspend fun checkAndUnlock(id: String, condition: suspend () -> Boolean) {
        if (achievementRepository.isUnlocked(id)) return
        if (condition()) {
            achievementRepository.unlock(id)
            checkMasterElectrician()
        }
    }
}
