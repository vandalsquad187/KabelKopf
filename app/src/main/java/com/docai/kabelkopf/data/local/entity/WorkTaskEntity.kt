package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_tasks")
data class WorkTaskEntity(
    @PrimaryKey val id: String,
    val projectId: String?,
    val customerName: String,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val date: Long,
    val durationSeconds: Int,
    val notes: String,
    val voiceNotePath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isCompleted: Boolean,
    val materials: String = "",
    val tools: String = "",
    val solution: String = "",
    val rating: Int = 0
)
