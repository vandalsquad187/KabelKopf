package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ChecklistDao
import com.docai.kabelkopf.data.local.entity.ChecklistEntity
import com.docai.kabelkopf.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepository @Inject constructor(
    private val checklistDao: ChecklistDao
) {
    fun getAllChecklists(): Flow<List<ChecklistEntity>> = checklistDao.getAllChecklists()

    fun getItemsForChecklist(checklistId: String): Flow<List<ChecklistItemEntity>> =
        checklistDao.getItemsForChecklist(checklistId)

    suspend fun createChecklist(name: String, category: String, items: List<String>): ChecklistEntity {
        val now = System.currentTimeMillis()
        val checklist = ChecklistEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            createdAt = now,
            updatedAt = now
        )
        checklistDao.insertChecklist(checklist)
        items.forEachIndexed { index, text ->
            checklistDao.insertItem(ChecklistItemEntity(
                id = UUID.randomUUID().toString(),
                checklistId = checklist.id,
                text = text,
                isChecked = false,
                orderIndex = index
            ))
        }
        return checklist
    }

    suspend fun toggleItem(itemId: String, isChecked: Boolean) {
        checklistDao.setItemChecked(itemId, isChecked)
    }

    suspend fun deleteChecklist(id: String) {
        checklistDao.deleteItemsForChecklist(id)
        checklistDao.deleteChecklistById(id)
    }
}
