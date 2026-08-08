package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.WorkTaskDao
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: WorkTaskDao
) {
    fun getAllTasks(): Flow<List<WorkTaskEntity>> = taskDao.getAllTasks()

    fun getRecentTasks(): Flow<List<WorkTaskEntity>> = taskDao.getRecentTasks()

    fun getActiveTasks(): Flow<List<WorkTaskEntity>> = taskDao.getActiveTasks()

    fun getTasksByCategory(category: String): Flow<List<WorkTaskEntity>> =
        taskDao.getTasksByCategory(category)

    fun searchTasks(query: String): Flow<List<WorkTaskEntity>> = taskDao.searchTasks(query)

    fun getTasksByProjectId(projectId: String): Flow<List<WorkTaskEntity>> =
        taskDao.getTasksByProjectId(projectId)

    fun getAllCategories(): Flow<List<String>> = taskDao.getAllCategories()

    fun getTaskCount(): Flow<Int> = taskDao.getTaskCount()

    fun getCompletedTaskCount(): Flow<Int> = taskDao.getCompletedTaskCount()

    suspend fun getTaskById(id: String): WorkTaskEntity? = taskDao.getTaskById(id)

    suspend fun createTask(
        title: String,
        category: String,
        description: String,
        location: String,
        customerName: String,
        projectId: String?,
        durationSeconds: Int = 0
    ): WorkTaskEntity {
        val now = System.currentTimeMillis()
        val task = WorkTaskEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            customerName = customerName,
            title = title,
            category = category,
            description = description,
            location = location,
            date = now,
            durationSeconds = durationSeconds,
            notes = "",
            voiceNotePath = null,
            createdAt = now,
            updatedAt = now,
            isCompleted = false
        )
        taskDao.insertTask(task)
        return task
    }

    suspend fun updateTask(task: WorkTaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun completeTask(id: String) {
        val task = taskDao.getTaskById(id) ?: return
        taskDao.updateTask(task.copy(isCompleted = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(id: String) = taskDao.deleteTaskById(id)
}
