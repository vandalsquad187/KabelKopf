package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ErrorCauseDao
import com.docai.kabelkopf.data.local.entity.ErrorCauseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorCauseRepository @Inject constructor(
    private val errorCauseDao: ErrorCauseDao
) {
    fun getCausesForCategory(category: String): Flow<List<ErrorCauseEntity>> =
        errorCauseDao.getCausesForCategory(category)

    fun getAllCauses(): Flow<List<ErrorCauseEntity>> =
        errorCauseDao.getAllCauses()
}
