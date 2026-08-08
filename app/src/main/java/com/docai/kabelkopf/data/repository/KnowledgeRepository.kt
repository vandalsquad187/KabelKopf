package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.KnowledgeBaseDao
import com.docai.kabelkopf.data.local.entity.KnowledgeBaseEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeRepository @Inject constructor(
    private val knowledgeBaseDao: KnowledgeBaseDao
) {
    fun getAllEntries(): Flow<List<KnowledgeBaseEntity>> = knowledgeBaseDao.getAllEntries()

    fun searchEntries(query: String): Flow<List<KnowledgeBaseEntity>> =
        knowledgeBaseDao.searchEntries(query)

    fun getEntriesByCategory(category: String): Flow<List<KnowledgeBaseEntity>> =
        knowledgeBaseDao.getEntriesByCategory(category)

    fun getFavoriteEntries(): Flow<List<KnowledgeBaseEntity>> = knowledgeBaseDao.getFavoriteEntries()

    fun getAllCategories(): Flow<List<String>> = knowledgeBaseDao.getAllCategories()

    suspend fun getEntryById(id: String): KnowledgeBaseEntity? = knowledgeBaseDao.getEntryById(id)

    suspend fun getEntriesByCategoryOnce(category: String): List<KnowledgeBaseEntity> =
        knowledgeBaseDao.getEntriesByCategoryOnce(category)

    suspend fun searchEntriesOnce(query: String): List<KnowledgeBaseEntity> =
        knowledgeBaseDao.searchEntriesOnce(query)

    suspend fun findRelevantKnowledge(category: String, keywords: List<String>): List<KnowledgeBaseEntity> {
        val results = mutableSetOf<KnowledgeBaseEntity>()
        val catEntries = knowledgeBaseDao.getEntriesByCategoryOnce(category)
        results.addAll(catEntries)
        keywords.forEach { kw ->
            if (kw.length > 2) {
                results.addAll(knowledgeBaseDao.searchEntriesOnce(kw))
            }
        }
        return results.take(5)
    }

    suspend fun createEntry(
        title: String,
        content: String,
        tags: String,
        category: String,
        sourceTaskId: String? = null
    ): KnowledgeBaseEntity {
        val entry = KnowledgeBaseEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            tags = tags,
            category = category,
            isFavorite = false,
            sourceTaskId = sourceTaskId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        knowledgeBaseDao.insertEntry(entry)
        return entry
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        knowledgeBaseDao.setFavorite(id, isFavorite)
    }

    suspend fun updateEntry(
        id: String,
        title: String,
        content: String,
        tags: String,
        category: String
    ) {
        val existing = knowledgeBaseDao.getEntryById(id) ?: return
        knowledgeBaseDao.updateEntry(
            existing.copy(
                title = title,
                content = content,
                tags = tags,
                category = category,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteEntry(id: String) = knowledgeBaseDao.deleteEntryById(id)
}
