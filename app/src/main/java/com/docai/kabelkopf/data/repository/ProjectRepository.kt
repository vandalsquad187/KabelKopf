package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ProjectDao
import com.docai.kabelkopf.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun createProject(
        name: String,
        address: String,
        contactName: String,
        contactPhone: String
    ): ProjectEntity {
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            address = address,
            contactName = contactName,
            contactPhone = contactPhone,
            createdAt = System.currentTimeMillis()
        )
        projectDao.insertProject(project)
        return project
    }

    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)

    suspend fun deleteProject(id: String) = projectDao.deleteProjectById(id)
}
