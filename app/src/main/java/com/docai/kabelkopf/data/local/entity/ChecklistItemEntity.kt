package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val checklistId: String,
    val text: String,
    val isChecked: Boolean,
    val orderIndex: Int
)
