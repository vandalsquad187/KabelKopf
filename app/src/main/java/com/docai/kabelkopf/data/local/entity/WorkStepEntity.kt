package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_steps")
data class WorkStepEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val stepOrder: Int,
    val description: String,
    val isDone: Boolean,
    val warning: String?,
    val imagePath: String?,
    val durationSeconds: Int
)
