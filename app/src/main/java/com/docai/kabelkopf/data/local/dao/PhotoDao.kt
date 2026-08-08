package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun getPhotosForTask(taskId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE stepId = :stepId ORDER BY createdAt ASC")
    fun getPhotosForStep(stepId: String): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: String)

    @Query("DELETE FROM photos WHERE taskId = :taskId")
    suspend fun deletePhotosForTask(taskId: String)
}
