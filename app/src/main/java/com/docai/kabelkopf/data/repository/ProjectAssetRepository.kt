package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ProjectAssetDao
import com.docai.kabelkopf.data.local.entity.ProjectAssetEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectAssetRepository @Inject constructor(
    private val projectAssetDao: ProjectAssetDao
) {
    fun getAssetsByProjectId(projectId: String): Flow<List<ProjectAssetEntity>> =
        projectAssetDao.getAssetsByProjectId(projectId)

    suspend fun getAssetById(id: String): ProjectAssetEntity? =
        projectAssetDao.getAssetById(id)

    suspend fun createAsset(
        projectId: String,
        type: String,
        title: String,
        location: String = "",
        fuseType: String = "",
        circuitInfo: String = "",
        uvType: String = "",
        description: String = "",
        notes: String = "",
        photoPaths: String = "",
        docPaths: String = ""
    ): ProjectAssetEntity {
        val asset = ProjectAssetEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            type = type,
            title = title,
            location = location,
            fuseType = fuseType,
            circuitInfo = circuitInfo,
            uvType = uvType,
            description = description,
            notes = notes,
            photoPaths = photoPaths,
            docPaths = docPaths,
            createdAt = System.currentTimeMillis()
        )
        projectAssetDao.insertAsset(asset)
        return asset
    }

    suspend fun updateAsset(asset: ProjectAssetEntity) =
        projectAssetDao.updateAsset(asset)

    suspend fun deleteAsset(id: String) =
        projectAssetDao.deleteAssetById(id)
}
