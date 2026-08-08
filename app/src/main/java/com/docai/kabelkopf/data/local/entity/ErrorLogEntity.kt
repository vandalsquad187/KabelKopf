package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey val id: String,
    val taskCategory: String,
    val description: String,
    val severity: Int,
    val date: Long,
    val taskId: String?,
    val wasAvoided: Boolean,
    val causes: String = "",
    val causeIds: String = "",
    val solution: String = ""
)
