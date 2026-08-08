package com.docai.kabelkopf.data.repository

import com.docai.kabelkopf.data.local.dao.ErrorLogDao
import com.docai.kabelkopf.data.local.dao.ErrorCauseDao
import com.docai.kabelkopf.data.local.entity.ErrorLogEntity
import com.docai.kabelkopf.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class CategoryErrorCount(
    val category: String,
    val count: Int,
    val severity: Int
)

data class CauseFrequency(
    val causeId: String,
    val label: String,
    val count: Int
)

data class CategoryInsight(
    val category: String,
    val totalErrors: Int,
    val topCauses: List<CauseFrequency>,
    val hasSolution: Boolean
)

data class AppInsights(
    val totalErrors: Int,
    val byCategory: List<CategoryErrorCount>,
    val topCausesOverall: List<CauseFrequency>,
    val lessonsLearned: List<ErrorLogEntity>,
    val categoryInsights: List<CategoryInsight>,
    val suggestion: String = ""
)

@Singleton
class InsightsRepository @Inject constructor(
    private val errorLogDao: ErrorLogDao,
    private val errorCauseDao: ErrorCauseDao
) {
    fun getErrorsWithSolutions(): Flow<List<ErrorLogEntity>> =
        errorLogDao.getErrorsWithSolutions()

    suspend fun computeInsights(): AppInsights {
        val allErrors = errorLogDao.getAllErrorsOnce()
        val allCauses = errorCauseDao.getAllCausesOnce()
        val causeLabels = allCauses.associate { it.id to it.label }

        val byCategory = allErrors
            .groupBy { it.taskCategory }
            .map { (cat, errors) ->
                CategoryErrorCount(cat, errors.size, errors.maxOf { it.severity })
            }
            .sortedByDescending { it.count }

        val causeCounts = allErrors
            .flatMap { it.causeIds.split(",").filter { id -> id.isNotBlank() } }
            .groupBy { it }
            .map { (id, occurrences) ->
                CauseFrequency(id, causeLabels[id] ?: "Unbekannt", occurrences.size)
            }
            .sortedByDescending { it.count }

        val lessons = allErrors.filter { it.solution.isNotBlank() }

        val categoryInsights = Constants.Categories.all.map { cat ->
            val catErrors = allErrors.filter { it.taskCategory == cat }
            val catCauseCounts = catErrors
                .flatMap { it.causeIds.split(",").filter { id -> id.isNotBlank() } }
                .groupBy { it }
                .map { (id, occurrences) ->
                    CauseFrequency(id, causeLabels[id] ?: "Unbekannt", occurrences.size)
                }
                .sortedByDescending { it.count }
            CategoryInsight(
                category = cat,
                totalErrors = catErrors.size,
                topCauses = catCauseCounts.take(3),
                hasSolution = catErrors.any { it.solution.isNotBlank() }
            )
        }.filter { it.totalErrors > 0 }

        val suggestion = buildSuggestion(byCategory, causeCounts)

        return AppInsights(
            totalErrors = allErrors.size,
            byCategory = byCategory,
            topCausesOverall = causeCounts,
            lessonsLearned = lessons,
            categoryInsights = categoryInsights,
            suggestion = suggestion
        )
    }

    private fun buildSuggestion(
        byCategory: List<CategoryErrorCount>,
        causeCounts: List<CauseFrequency>
    ): String {
        if (byCategory.isEmpty()) return ""
        val worst = byCategory.first()
        val topCause = causeCounts.firstOrNull()
        return if (topCause != null) {
            "🔍 Bei **${worst.category}** hast du die meisten Fehler " +
                "(${worst.count}). Häufigste Ursache: **${topCause.label}** " +
                "(${topCause.count}x). Erstelle eine Checkliste für " +
                "${worst.category}, um diesen Fehler zu vermeiden!"
        } else {
            "🔍 Bei **${worst.category}** hast du die meisten Fehler " +
                "(${worst.count}). Analysiere die Ursachen und dokumentiere " +
                "Lösungen, um beim nächsten Mal besser zu sein!"
        }
    }
}
