package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.KnowledgeBaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeBaseDao {
    @Query("SELECT * FROM knowledge_base ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE category = :category ORDER BY title ASC")
    fun getEntriesByCategory(category: String): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE category = :category ORDER BY title ASC")
    suspend fun getEntriesByCategoryOnce(category: String): List<KnowledgeBaseEntity>

    @Query("SELECT * FROM knowledge_base WHERE id = :id")
    suspend fun getEntryById(id: String): KnowledgeBaseEntity?

    @Query(
        """
        SELECT * FROM knowledge_base 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%' 
        OR tags LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
        """
    )
    fun searchEntries(query: String): Flow<List<KnowledgeBaseEntity>>

    @Query(
        """
        SELECT * FROM knowledge_base 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%' 
        OR tags LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
        """
    )
    suspend fun searchEntriesOnce(query: String): List<KnowledgeBaseEntity>

    @Query("SELECT * FROM knowledge_base WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteEntries(): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT DISTINCT category FROM knowledge_base ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: KnowledgeBaseEntity)

    @Update
    suspend fun updateEntry(entry: KnowledgeBaseEntity)

    @Delete
    suspend fun deleteEntry(entry: KnowledgeBaseEntity)

    @Query("UPDATE knowledge_base SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM knowledge_base WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM knowledge_base")
    suspend fun deleteAll()
}
