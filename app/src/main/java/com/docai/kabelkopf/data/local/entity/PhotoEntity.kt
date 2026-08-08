package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val taskId: String?,
    val stepId: String?,
    val filePath: String,
    val caption: String,
    val createdAt: Long
)
