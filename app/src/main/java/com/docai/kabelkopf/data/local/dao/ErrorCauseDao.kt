package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.ErrorCauseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorCauseDao {
    @Query("SELECT * FROM error_causes WHERE category = '' OR category = :category ORDER BY label ASC")
    fun getCausesForCategory(category: String): Flow<List<ErrorCauseEntity>>

    @Query("SELECT * FROM error_causes ORDER BY label ASC")
    fun getAllCauses(): Flow<List<ErrorCauseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCause(cause: ErrorCauseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(causes: List<ErrorCauseEntity>)

    @Delete
    suspend fun deleteCause(cause: ErrorCauseEntity)

    @Query("SELECT * FROM error_causes WHERE id = :id")
    suspend fun getCauseById(id: String): ErrorCauseEntity?

    @Query("SELECT * FROM error_causes")
    suspend fun getAllCausesOnce(): List<ErrorCauseEntity>

    @Query("DELETE FROM error_causes")
    suspend fun deleteAll()
}
