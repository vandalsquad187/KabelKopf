package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkTaskDao {
    @Query("SELECT * FROM work_tasks ORDER BY date DESC")
    fun getAllTasks(): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): WorkTaskEntity?

    @Query("SELECT * FROM work_tasks ORDER BY date DESC LIMIT 5")
    fun getRecentTasks(): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE category = :category ORDER BY date DESC")
    fun getTasksByCategory(category: String): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE customerName LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTasks(query: String): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR materials LIKE '%' || :query || '%' OR tools LIKE '%' || :query || '%' OR solution LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchTasksAllFields(query: String): List<WorkTaskEntity>

    @Query("SELECT * FROM work_tasks WHERE projectId = :projectId ORDER BY date DESC")
    fun getTasksByProjectId(projectId: String): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE isCompleted = 0 ORDER BY date DESC")
    fun getActiveTasks(): Flow<List<WorkTaskEntity>>

    @Query("SELECT DISTINCT category FROM work_tasks ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WorkTaskEntity)

    @Update
    suspend fun updateTask(task: WorkTaskEntity)

    @Delete
    suspend fun deleteTask(task: WorkTaskEntity)

    @Query("DELETE FROM work_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT COUNT(*) FROM work_tasks")
    fun getTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM work_tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM work_tasks")
    suspend fun getTaskCountOnce(): Int

    @Query("SELECT DISTINCT category FROM work_tasks")
    suspend fun getAllUsedCategoriesOnce(): List<String>

    @Query("SELECT date FROM work_tasks ORDER BY date DESC")
    suspend fun getAllTaskDates(): List<Long>

    @Query("DELETE FROM work_tasks")
    suspend fun deleteAll()
}
