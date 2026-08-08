package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflow_templates")
data class WorkflowTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val stepsJson: String,
    val isBuiltIn: Boolean,
    val usageCount: Int,
    val createdAt: Long
)
