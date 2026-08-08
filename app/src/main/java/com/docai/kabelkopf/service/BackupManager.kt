package com.docai.kabelkopf.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.docai.kabelkopf.data.local.dao.*
import com.docai.kabelkopf.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workTaskDao: WorkTaskDao,
    private val workStepDao: WorkStepDao,
    private val errorLogDao: ErrorLogDao,
    private val errorCauseDao: ErrorCauseDao,
    private val knowledgeBaseDao: KnowledgeBaseDao,
    private val projectDao: ProjectDao,
    private val skillDao: SkillDao,
    private val achievementDao: AchievementDao,
    private val workflowTemplateDao: WorkflowTemplateDao,
    private val photoDao: PhotoDao
) {

    suspend fun exportBackup(): File? {
        val backup = JSONObject()

        backup.put("app", "GesellenGedächtnis")
        backup.put("exportDate", System.currentTimeMillis())
        backup.put("version", 1)

        backup.put("tasks", toJsonArray(workTaskDao.getAllTasks().first()) { it.toJson() })
        backup.put("steps", toJsonArray(workStepDao.getAllSteps().first()) { it.toJson() })
        backup.put("errors", toJsonArray(errorLogDao.getAllErrors().first()) { it.toJson() })
        backup.put("errorCauses", toJsonArray(errorCauseDao.getAllCauses().first()) { it.toJson() })
        backup.put("knowledge", toJsonArray(knowledgeBaseDao.getAllEntries().first()) { it.toJson() })
        backup.put("projects", toJsonArray(projectDao.getAllProjects().first()) { it.toJson() })
        backup.put("skills", toJsonArray(skillDao.getAllSkills().first()) { it.toJson() })
        backup.put("achievements", toJsonArray(achievementDao.getAllAchievements().first()) { it.toJson() })
        backup.put("templates", toJsonArray(workflowTemplateDao.getAllTemplates().first()) { it.toJson() })

        val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.GERMANY).format(Date())
        val file = File(context.cacheDir, "GesellenGedaechtnis_Backup_$dateStr.json")
        file.writeText(backup.toString(2))
        return file
    }

    suspend fun importBackup(uri: Uri): Boolean {
        return try {
            val reader = BufferedReader(
                InputStreamReader(context.contentResolver.openInputStream(uri) ?: return false)
            )
            val json = JSONObject(reader.readText())
            reader.close()

            clearAll()

            parseAndInsert(json.optJSONArray("tasks")) { insertTask(it) }
            parseAndInsert(json.optJSONArray("steps")) { insertStep(it) }
            parseAndInsert(json.optJSONArray("errors")) { insertErrorLog(it) }
            parseAndInsert(json.optJSONArray("errorCauses")) { insertErrorCause(it) }
            parseAndInsert(json.optJSONArray("knowledge")) { insertKnowledge(it) }
            parseAndInsert(json.optJSONArray("projects")) { insertProject(it) }
            parseAndInsert(json.optJSONArray("skills")) { insertSkill(it) }
            parseAndInsert(json.optJSONArray("achievements")) { insertAchievement(it) }
            parseAndInsert(json.optJSONArray("templates")) { insertTemplate(it) }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun parseAndInsert(arr: JSONArray?, insert: suspend (JSONObject) -> Unit) {
        if (arr == null) return
        for (i in 0 until arr.length()) {
            insert(arr.getJSONObject(i))
        }
    }

    private suspend fun clearAll() {
        workTaskDao.deleteAll()
        workStepDao.deleteAll()
        errorLogDao.deleteAll()
        errorCauseDao.deleteAll()
        knowledgeBaseDao.deleteAll()
        projectDao.deleteAll()
        skillDao.deleteAll()
        achievementDao.deleteAll()
        workflowTemplateDao.deleteAll()
    }

    private suspend fun insertTask(obj: JSONObject) {
        workTaskDao.insertTask(WorkTaskEntity(
            id = obj.getString("id"),
            title = obj.optString("title"),
            category = obj.optString("category"),
            description = obj.optString("description"),
            location = obj.optString("location"),
            customerName = obj.optString("customerName"),
            date = obj.optLong("date"),
            isCompleted = obj.optBoolean("isCompleted"),
            projectId = obj.optString("projectId", null),
            durationSeconds = obj.optInt("durationSeconds"),
            notes = obj.optString("notes"),
            voiceNotePath = obj.optString("voiceNotePath", null),
            createdAt = obj.optLong("createdAt", obj.optLong("date")),
            updatedAt = obj.optLong("updatedAt", obj.optLong("date")),
            materials = obj.optString("materials"),
            tools = obj.optString("tools"),
            solution = obj.optString("solution"),
            rating = obj.optInt("rating")
        ))
    }

    private suspend fun insertStep(obj: JSONObject) {
        workStepDao.insertStep(WorkStepEntity(
            id = obj.getString("id"),
            taskId = obj.getString("taskId"),
            stepOrder = obj.optInt("stepOrder"),
            description = obj.optString("description"),
            isDone = obj.optBoolean("isDone"),
            warning = obj.optString("warning", null),
            imagePath = null,
            durationSeconds = obj.optInt("durationSeconds")
        ))
    }

    private suspend fun insertErrorLog(obj: JSONObject) {
        errorLogDao.insertError(ErrorLogEntity(
            id = obj.getString("id"),
            taskCategory = obj.optString("taskCategory"),
            description = obj.optString("description"),
            severity = obj.optInt("severity"),
            date = obj.optLong("date"),
            taskId = obj.optString("taskId", null),
            wasAvoided = obj.optBoolean("wasAvoided"),
            causes = obj.optString("causes"),
            causeIds = obj.optString("causeIds"),
            solution = obj.optString("solution")
        ))
    }

    private suspend fun insertErrorCause(obj: JSONObject) {
        errorCauseDao.insertCause(ErrorCauseEntity(
            id = obj.getString("id"),
            label = obj.optString("label"),
            description = obj.optString("description"),
            category = obj.optString("category")
        ))
    }

    private suspend fun insertKnowledge(obj: JSONObject) {
        knowledgeBaseDao.insertEntry(KnowledgeBaseEntity(
            id = obj.getString("id"),
            title = obj.optString("title"),
            content = obj.optString("content"),
            tags = obj.optString("tags"),
            category = obj.optString("category"),
            isFavorite = obj.optBoolean("isFavorite"),
            sourceTaskId = null,
            createdAt = obj.optLong("createdAt"),
            updatedAt = obj.optLong("updatedAt")
        ))
    }

    private suspend fun insertProject(obj: JSONObject) {
        projectDao.insertProject(ProjectEntity(
            id = obj.getString("id"),
            name = obj.optString("name"),
            address = obj.optString("address"),
            contactName = obj.optString("contactName"),
            contactPhone = obj.optString("contactPhone"),
            createdAt = obj.optLong("createdAt")
        ))
    }

    private suspend fun insertSkill(obj: JSONObject) {
        skillDao.insertSkill(SkillEntity(
            id = obj.getString("id"),
            name = obj.optString("name"),
            category = obj.optString("category"),
            description = obj.optString("description"),
            currentXp = obj.optInt("currentXp"),
            level = obj.optInt("level"),
            maxLevel = obj.optInt("maxLevel"),
            nextLevelXp = obj.optInt("nextLevelXp")
        ))
    }

    private suspend fun insertAchievement(obj: JSONObject) {
        achievementDao.insertAll(listOf(AchievementEntity(
            id = obj.getString("id"),
            name = obj.optString("name"),
            description = obj.optString("description"),
            tier = obj.optInt("tier"),
            isUnlocked = obj.optBoolean("isUnlocked"),
            unlockedAt = if (obj.isNull("unlockedAt")) null else obj.optLong("unlockedAt"),
            isBuiltIn = true
        )))
    }

    private suspend fun insertTemplate(obj: JSONObject) {
        workflowTemplateDao.insertTemplate(WorkflowTemplateEntity(
            id = obj.getString("id"),
            name = obj.optString("name"),
            category = obj.optString("category"),
            stepsJson = obj.optString("stepsJson"),
            isBuiltIn = obj.optBoolean("isBuiltIn"),
            usageCount = obj.optInt("usageCount"),
            createdAt = obj.optLong("createdAt")
        ))
    }

    fun shareBackup(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Backup teilen"))
    }

    private fun <T> toJsonArray(items: List<T>, transform: (T) -> JSONObject): JSONArray {
        val arr = JSONArray()
        items.forEach { arr.put(transform(it)) }
        return arr
    }

    @Suppress("DEPRECATION")
    private fun WorkTaskEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("category", category)
        put("description", description)
        put("location", location)
        put("customerName", customerName)
        put("date", date)
        put("isCompleted", isCompleted)
        put("projectId", projectId ?: JSONObject.NULL)
        put("durationSeconds", durationSeconds)
        put("notes", notes)
        put("voiceNotePath", voiceNotePath ?: JSONObject.NULL)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("materials", materials)
        put("tools", tools)
        put("solution", solution)
        put("rating", rating)
    }

    @Suppress("DEPRECATION")
    private fun WorkStepEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("taskId", taskId)
        put("stepOrder", stepOrder)
        put("description", description)
        put("isDone", isDone)
        put("warning", warning ?: JSONObject.NULL)
        put("durationSeconds", durationSeconds)
    }

    @Suppress("DEPRECATION")
    private fun ErrorLogEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("taskCategory", taskCategory)
        put("description", description)
        put("severity", severity)
        put("date", date)
        put("taskId", taskId ?: JSONObject.NULL)
        put("wasAvoided", wasAvoided)
        put("causes", causes)
        put("causeIds", causeIds)
        put("solution", solution)
    }

    @Suppress("DEPRECATION")
    private fun ErrorCauseEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("description", description)
        put("category", category)
    }

    @Suppress("DEPRECATION")
    private fun KnowledgeBaseEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("tags", tags)
        put("category", category)
        put("isFavorite", isFavorite)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    @Suppress("DEPRECATION")
    private fun ProjectEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("address", address)
        put("contactName", contactName)
        put("contactPhone", contactPhone)
        put("createdAt", createdAt)
    }

    @Suppress("DEPRECATION")
    private fun SkillEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("description", description)
        put("currentXp", currentXp)
        put("level", level)
        put("maxLevel", maxLevel)
        put("nextLevelXp", nextLevelXp)
    }

    @Suppress("DEPRECATION")
    private fun AchievementEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("tier", tier)
        put("isUnlocked", isUnlocked)
        put("unlockedAt", unlockedAt ?: JSONObject.NULL)
    }

    @Suppress("DEPRECATION")
    private fun WorkflowTemplateEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("stepsJson", stepsJson)
        put("isBuiltIn", isBuiltIn)
        put("usageCount", usageCount)
        put("createdAt", createdAt)
    }
}
