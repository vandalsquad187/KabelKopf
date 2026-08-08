package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.ProjectAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectAssetDao {
    @Query("SELECT * FROM project_assets WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getAssetsByProjectId(projectId: String): Flow<List<ProjectAssetEntity>>

    @Query("SELECT * FROM project_assets WHERE id = :id")
    suspend fun getAssetById(id: String): ProjectAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: ProjectAssetEntity)

    @Update
    suspend fun updateAsset(asset: ProjectAssetEntity)

    @Delete
    suspend fun deleteAsset(asset: ProjectAssetEntity)

    @Query("DELETE FROM project_assets WHERE id = :id")
    suspend fun deleteAssetById(id: String)

    @Query("DELETE FROM project_assets WHERE projectId = :projectId")
    suspend fun deleteAssetsByProjectId(projectId: String)

    @Query("DELETE FROM project_assets")
    suspend fun deleteAll()
}
