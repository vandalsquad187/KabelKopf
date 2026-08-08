package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.WorkflowTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowTemplateDao {
    @Query("SELECT * FROM workflow_templates ORDER BY usageCount DESC")
    fun getAllTemplates(): Flow<List<WorkflowTemplateEntity>>

    @Query("SELECT * FROM workflow_templates WHERE category = :category ORDER BY usageCount DESC")
    fun getTemplatesByCategory(category: String): Flow<List<WorkflowTemplateEntity>>

    @Query("SELECT * FROM workflow_templates WHERE id = :id")
    suspend fun getTemplateById(id: String): WorkflowTemplateEntity?

    @Query("SELECT * FROM workflow_templates WHERE isBuiltIn = 1")
    fun getBuiltInTemplates(): Flow<List<WorkflowTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkflowTemplateEntity)

    @Update
    suspend fun updateTemplate(template: WorkflowTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: WorkflowTemplateEntity)

    @Query("UPDATE workflow_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: String)

    @Query("DELETE FROM workflow_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: String)

    @Query("DELETE FROM workflow_templates")
    suspend fun deleteAll()
}
