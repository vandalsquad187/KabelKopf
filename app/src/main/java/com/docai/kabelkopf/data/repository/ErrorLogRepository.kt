package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ErrorLogDao
import com.docai.kabelkopf.data.local.entity.ErrorLogEntity
import com.docai.kabelkopf.util.Constants
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorLogRepository @Inject constructor(
    private val errorLogDao: ErrorLogDao
) {
    fun getAllErrors(): Flow<List<ErrorLogEntity>> = errorLogDao.getAllErrors()

    fun getErrorsByCategory(category: String): Flow<List<ErrorLogEntity>> =
        errorLogDao.getErrorsByCategory(category)

    suspend fun getRelevantWarnings(category: String): List<ErrorLogEntity> {
        val sinceTimestamp = System.currentTimeMillis() - (Constants.WARNING_LOOKBACK_DAYS * 86400000L)
        return errorLogDao.getRelevantWarnings(
            category = category,
            minSeverity = 2,
            sinceTimestamp = sinceTimestamp
        ).filter { it.severity >= Constants.WARNING_SEVERITY_THRESHOLD }
    }

    suspend fun logError(
        taskCategory: String,
        description: String,
        severity: Int,
        taskId: String? = null,
        causeIds: List<String> = emptyList(),
        solution: String = ""
    ): ErrorLogEntity {
        val error = ErrorLogEntity(
            id = UUID.randomUUID().toString(),
            taskCategory = taskCategory,
            description = description,
            severity = severity,
            date = System.currentTimeMillis(),
            taskId = taskId,
            wasAvoided = false,
            causeIds = causeIds.joinToString(","),
            solution = solution
        )
        errorLogDao.insertError(error)
        return error
    }

    suspend fun getErrorById(id: String): ErrorLogEntity? =
        errorLogDao.getErrorById(id)

    suspend fun getErrorCountByTask(taskId: String): Int =
        errorLogDao.getErrorCountByTask(taskId)

    suspend fun getErrorsWithSolutionCount(): Int =
        errorLogDao.getErrorsWithSolutionCount()

    fun getErrorStats(): Flow<List<com.docai.kabelkopf.data.local.dao.ErrorCategoryStats>> =
        errorLogDao.getErrorStatsByCategory()
}
