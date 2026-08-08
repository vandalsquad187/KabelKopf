package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.WorkStepDao
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: WorkStepDao
) {
    fun getStepsForTask(taskId: String): Flow<List<WorkStepEntity>> =
        stepDao.getStepsForTask(taskId)

    suspend fun addStep(
        taskId: String,
        description: String,
        order: Int,
        warning: String? = null
    ): WorkStepEntity {
        val step = WorkStepEntity(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            stepOrder = order,
            description = description,
            isDone = false,
            warning = warning,
            imagePath = null,
            durationSeconds = 0
        )
        stepDao.insertStep(step)
        return step
    }

    suspend fun addStepsFromTemplate(taskId: String, stepDescriptions: List<String>) {
        val steps = stepDescriptions.mapIndexed { index, desc ->
            WorkStepEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                stepOrder = index,
                description = desc,
                isDone = false,
                warning = null,
                imagePath = null,
                durationSeconds = 0
            )
        }
        stepDao.insertSteps(steps)
    }

    suspend fun setStepDone(stepId: String, isDone: Boolean) =
        stepDao.setStepDone(stepId, isDone)

    suspend fun deleteStepsForTask(taskId: String) = stepDao.deleteStepsForTask(taskId)

    suspend fun getStepsForTaskOnce(taskId: String): List<WorkStepEntity> =
        stepDao.getStepsForTaskOnce(taskId)
}
