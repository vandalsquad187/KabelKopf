package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long
)
