package com.elektriker.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_assets")
data class ProjectAssetEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val type: String,
    val title: String,
    val location: String,
    @ColumnInfo(name = "fuse_type") val fuseType: String,
    @ColumnInfo(name = "circuit_info") val circuitInfo: String,
    @ColumnInfo(name = "uv_type") val uvType: String,
    val description: String,
    val notes: String,
    @ColumnInfo(name = "photo_paths") val photoPaths: String,
    @ColumnInfo(name = "doc_paths") val docPaths: String,
    val createdAt: Long
)
