package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.*
import com.docai.kabelkopf.data.local.entity.*
import javax.inject.Inject
import javax.inject.Singleton

data class GlobalSearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String
)

@Singleton
class SearchRepository @Inject constructor(
    private val workTaskDao: WorkTaskDao,
    private val knowledgeBaseDao: KnowledgeBaseDao,
    private val errorLogDao: ErrorLogDao,
    private val projectDao: ProjectDao
) {
    suspend fun searchAll(query: String): List<GlobalSearchResult> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<GlobalSearchResult>()

        workTaskDao.searchTasksAllFields(query).forEach { task ->
            results.add(GlobalSearchResult(
                id = task.id,
                title = task.title,
                subtitle = "${task.category} · ${task.location}",
                type = "Arbeit"
            ))
        }

        knowledgeBaseDao.searchEntriesOnce(query).forEach { entry ->
            results.add(GlobalSearchResult(
                id = entry.id,
                title = entry.title,
                subtitle = "Wissen · ${entry.category}",
                type = "Wissen"
            ))
        }

        errorLogDao.searchErrors(query).forEach { error ->
            results.add(GlobalSearchResult(
                id = error.id,
                title = error.description,
                subtitle = "Fehler · ${error.taskCategory}",
                type = "Fehler"
            ))
        }

        projectDao.searchProjects(query).forEach { project ->
            results.add(GlobalSearchResult(
                id = project.id,
                title = project.name,
                subtitle = "Projekt · ${project.address}",
                type = "Projekt"
            ))
        }

        results.sortByDescending { it.type }
        return results
    }

    suspend fun searchTasksByQuery(query: String): List<WorkTaskEntity> =
        workTaskDao.searchTasksAllFields(query)
}
