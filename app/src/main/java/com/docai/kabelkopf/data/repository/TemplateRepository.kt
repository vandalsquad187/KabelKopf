package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.WorkflowTemplateDao
import com.docai.kabelkopf.data.local.entity.WorkflowTemplateEntity
import com.docai.kabelkopf.util.Constants
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: WorkflowTemplateDao
) {
    fun getAllTemplates(): Flow<List<WorkflowTemplateEntity>> = templateDao.getAllTemplates()

    fun getTemplatesByCategory(category: String): Flow<List<WorkflowTemplateEntity>> =
        templateDao.getTemplatesByCategory(category)

    suspend fun getTemplateById(id: String): WorkflowTemplateEntity? =
        templateDao.getTemplateById(id)

    suspend fun createTemplate(
        name: String,
        category: String,
        steps: List<String>,
        isBuiltIn: Boolean = false
    ): WorkflowTemplateEntity {
        val template = WorkflowTemplateEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            stepsJson = steps.joinToString("\n---\n"),
            isBuiltIn = isBuiltIn,
            usageCount = 0,
            createdAt = System.currentTimeMillis()
        )
        templateDao.insertTemplate(template)
        return template
    }

    suspend fun incrementUsage(id: String) = templateDao.incrementUsage(id)

    suspend fun seedBuiltInTemplates() {
        templateDao.insertTemplate(
            WorkflowTemplateEntity(
                id = UUID.randomUUID().toString(),
                name = "Unterverteilung verdrahten",
                category = Constants.Categories.UV,
                stepsJson = Constants.BuiltInTemplates.uvVerdrahten.joinToString("\n---\n"),
                isBuiltIn = true,
                usageCount = 0,
                createdAt = System.currentTimeMillis()
            )
        )
        templateDao.insertTemplate(
            WorkflowTemplateEntity(
                id = UUID.randomUUID().toString(),
                name = "RCD/FI-Schalter prüfen",
                category = Constants.Categories.RCD,
                stepsJson = Constants.BuiltInTemplates.rcdPruefen.joinToString("\n---\n"),
                isBuiltIn = true,
                usageCount = 0,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
