package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_causes")
data class ErrorCauseEntity(
    @PrimaryKey val id: String,
    val label: String,
    val description: String = "",
    val category: String = ""
)
