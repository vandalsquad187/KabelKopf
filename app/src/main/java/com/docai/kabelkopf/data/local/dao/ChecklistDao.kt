package com.docai.kabelkopf.data.local.dao

import androidx.room.*
import com.docai.kabelkopf.data.local.entity.ChecklistEntity
import com.docai.kabelkopf.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists ORDER BY updatedAt DESC")
    fun getAllChecklists(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getChecklistById(id: String): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: ChecklistEntity)

    @Update
    suspend fun updateChecklist(checklist: ChecklistEntity)

    @Delete
    suspend fun deleteChecklist(checklist: ChecklistEntity)

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun deleteChecklistById(id: String)

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY orderIndex ASC")
    fun getItemsForChecklist(checklistId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY orderIndex ASC")
    suspend fun getItemsForChecklistOnce(checklistId: String): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun setItemChecked(id: String, isChecked: Boolean)

    @Delete
    suspend fun deleteItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun deleteItemsForChecklist(checklistId: String)

    @Query("DELETE FROM checklists")
    suspend fun deleteAll()

    @Query("DELETE FROM checklist_items")
    suspend fun deleteAllItems()
}
