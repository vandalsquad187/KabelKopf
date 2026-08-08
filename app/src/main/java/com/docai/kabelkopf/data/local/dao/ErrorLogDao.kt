package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorLogDao {
    @Query("SELECT * FROM error_logs ORDER BY date DESC")
    fun getAllErrors(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE taskCategory = :category ORDER BY date DESC")
    fun getErrorsByCategory(category: String): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE taskCategory = :category AND date > :sinceTimestamp ORDER BY date DESC")
    fun getRecentErrorsByCategory(category: String, sinceTimestamp: Long): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE severity >= :minSeverity ORDER BY date DESC")
    fun getHighSeverityErrors(minSeverity: Int): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE taskCategory = :category AND severity >= :minSeverity AND date > :sinceTimestamp")
    suspend fun getRelevantWarnings(category: String, minSeverity: Int, sinceTimestamp: Long): List<ErrorLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertError(error: ErrorLogEntity)

    @Update
    suspend fun updateError(error: ErrorLogEntity)

    @Delete
    suspend fun deleteError(error: ErrorLogEntity)

    @Query("SELECT taskCategory, COUNT(*) as count, MAX(severity) as maxSeverity FROM error_logs GROUP BY taskCategory")
    fun getErrorStatsByCategory(): Flow<List<ErrorCategoryStats>>

    @Query("DELETE FROM error_logs WHERE id = :id")
    suspend fun deleteErrorById(id: String)

    @Query("SELECT * FROM error_logs WHERE taskId = :taskId ORDER BY date DESC")
    fun getErrorsByTaskId(taskId: String): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE id = :id")
    suspend fun getErrorById(id: String): ErrorLogEntity?

    @Query("SELECT * FROM error_logs WHERE solution != '' ORDER BY date DESC")
    fun getErrorsWithSolutions(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE description LIKE '%' || :query || '%' OR causes LIKE '%' || :query || '%' OR solution LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchErrors(query: String): List<ErrorLogEntity>

    @Query("SELECT * FROM error_logs")
    suspend fun getAllErrorsOnce(): List<ErrorLogEntity>

    @Query("SELECT COUNT(*) FROM error_logs WHERE taskCategory = :category")
    suspend fun getErrorCountByCategory(category: String): Int

    @Query("SELECT COUNT(*) FROM error_logs WHERE taskId = :taskId")
    suspend fun getErrorCountByTask(taskId: String): Int

    @Query("SELECT COUNT(*) FROM error_logs WHERE solution != ''")
    suspend fun getErrorsWithSolutionCount(): Int

    @Query("DELETE FROM error_logs")
    suspend fun deleteAll()
}

data class ErrorCategoryStats(
    val taskCategory: String,
    val count: Int,
    val maxSeverity: Int
)
