package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkStepDao {
    @Query("SELECT * FROM work_steps WHERE taskId = :taskId ORDER BY stepOrder ASC")
    fun getStepsForTask(taskId: String): Flow<List<WorkStepEntity>>

    @Query("SELECT * FROM work_steps ORDER BY taskId ASC, stepOrder ASC")
    fun getAllSteps(): Flow<List<WorkStepEntity>>

    @Query("SELECT * FROM work_steps WHERE taskId = :taskId ORDER BY stepOrder ASC")
    suspend fun getStepsForTaskOnce(taskId: String): List<WorkStepEntity>

    @Query("SELECT * FROM work_steps WHERE id = :id")
    suspend fun getStepById(id: String): WorkStepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: WorkStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<WorkStepEntity>)

    @Update
    suspend fun updateStep(step: WorkStepEntity)

    @Query("UPDATE work_steps SET isDone = :isDone WHERE id = :stepId")
    suspend fun setStepDone(stepId: String, isDone: Boolean)

    @Delete
    suspend fun deleteStep(step: WorkStepEntity)

    @Query("DELETE FROM work_steps WHERE taskId = :taskId")
    suspend fun deleteStepsForTask(taskId: String)

    @Query("SELECT COUNT(*) FROM work_steps WHERE taskId = :taskId AND isDone = 1")
    suspend fun getCompletedStepCount(taskId: String): Int

    @Query("SELECT COUNT(*) FROM work_steps WHERE taskId = :taskId")
    suspend fun getTotalStepCount(taskId: String): Int

    @Query("DELETE FROM work_steps")
    suspend fun deleteAll()
}
