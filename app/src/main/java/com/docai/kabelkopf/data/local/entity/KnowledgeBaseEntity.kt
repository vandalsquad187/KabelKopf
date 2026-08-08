package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_base")
data class KnowledgeBaseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val tags: String,
    val category: String,
    val isFavorite: Boolean,
    val sourceTaskId: String?,
    val createdAt: Long,
    val updatedAt: Long
)
