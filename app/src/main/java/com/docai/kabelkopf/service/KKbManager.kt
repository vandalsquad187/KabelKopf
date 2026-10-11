package com.docai.kabelkopf.service

import android.content.Context
import android.net.Uri
import com.docai.kabelkopf.BuildConfig
import com.docai.kabelkopf.data.local.dao.*
import com.docai.kabelkopf.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ExportSelection(
    val knowledge: Boolean = true,
    val errors: Boolean = true,
    val errorCauses: Boolean = true,
    val projects: Boolean = true,
    val tasks: Boolean = true,
    val steps: Boolean = true,
    val skills: Boolean = true,
    val achievements: Boolean = true,
    val templates: Boolean = true,
    val photos: Boolean = true,
    val checklists: Boolean = true,
    val checklistItems: Boolean = true,
    val projectAssets: Boolean = true
)

data class ImportPreview(
    val manifest: JSONObject,
    val knowledgeCount: Int,
    val errorsCount: Int,
    val errorCausesCount: Int,
    val projectsCount: Int,
    val tasksCount: Int,
    val stepsCount: Int,
    val skillsCount: Int,
    val achievementsCount: Int,
    val templatesCount: Int,
    val photosCount: Int,
    val checklistsCount: Int,
    val checklistItemsCount: Int,
    val projectAssetsCount: Int,
    val exportDate: String,
    val appVersion: String
)

@Singleton
class KKbManager @Inject constructor(
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
    private val photoDao: PhotoDao,
    private val checklistDao: ChecklistDao,
    private val projectAssetDao: ProjectAssetDao
) {

    suspend fun exportKKb(selection: ExportSelection, outputUri: Uri): Boolean {
        return try {
            val database = JSONObject()

            if (selection.knowledge) {
                database.put("knowledge", toJsonArray(knowledgeBaseDao.getAllEntries().first()) { it.toJson() })
            }
            if (selection.errors) {
                database.put("errors", toJsonArray(errorLogDao.getAllErrors().first()) { it.toJson() })
            }
            if (selection.errorCauses) {
                database.put("errorCauses", toJsonArray(errorCauseDao.getAllCauses().first()) { it.toJson() })
            }
            if (selection.projects) {
                database.put("projects", toJsonArray(projectDao.getAllProjects().first()) { it.toJson() })
            }
            if (selection.tasks) {
                database.put("tasks", toJsonArray(workTaskDao.getAllTasks().first()) { it.toJson() })
            }
            if (selection.steps) {
                database.put("steps", toJsonArray(workStepDao.getAllSteps().first()) { it.toJson() })
            }
            if (selection.skills) {
                database.put("skills", toJsonArray(skillDao.getAllSkills().first()) { it.toJson() })
            }
            if (selection.achievements) {
                database.put("achievements", toJsonArray(achievementDao.getAllAchievements().first()) { it.toJson() })
            }
            if (selection.templates) {
                database.put("templates", toJsonArray(workflowTemplateDao.getAllTemplates().first()) { it.toJson() })
            }
            if (selection.photos) {
                database.put("photos", toJsonArray(photoDao.getAllPhotos().first()) { it.toJson() })
            }
            if (selection.checklists) {
                database.put("checklists", toJsonArray(checklistDao.getAllChecklists().first()) { it.toJson() })
            }
            if (selection.checklistItems) {
                database.put("checklistItems", toJsonArray(checklistDao.getAllChecklistItems().first()) { it.toJson() })
            }
            if (selection.projectAssets) {
                database.put("projectAssets", toJsonArray(projectAssetDao.getAllAssets().first()) { it.toJson() })
            }

            val manifest = JSONObject().apply {
                put("app", "Kabel-Kopf")
                put("version", BuildConfig.VERSION_NAME)
                put("versionCode", BuildConfig.VERSION_CODE)
                put("exportDate", System.currentTimeMillis())
                put("contains", JSONObject().apply {
                    if (selection.knowledge) put("knowledge", knowledgeBaseDao.getAllEntries().first().size)
                    if (selection.errors) put("errors", errorLogDao.getAllErrors().first().size)
                    if (selection.errorCauses) put("errorCauses", errorCauseDao.getAllCauses().first().size)
                    if (selection.projects) put("projects", projectDao.getAllProjects().first().size)
                    if (selection.tasks) put("tasks", workTaskDao.getAllTasks().first().size)
                    if (selection.steps) put("steps", workStepDao.getAllSteps().first().size)
                    if (selection.skills) put("skills", skillDao.getAllSkills().first().size)
                    if (selection.achievements) put("achievements", achievementDao.getAllAchievements().first().size)
                    if (selection.templates) put("templates", workflowTemplateDao.getAllTemplates().first().size)
                    if (selection.photos) put("photos", photoDao.getAllPhotos().first().size)
                    if (selection.checklists) put("checklists", checklistDao.getAllChecklists().first().size)
                    if (selection.checklistItems) put("checklistItems", checklistDao.getAllChecklistItems().first().size)
                    if (selection.projectAssets) put("projectAssets", projectAssetDao.getAllAssets().first().size)
                })
            }

            val images = mutableMapOf<String, File>()
            if (selection.photos) {
                val photos = photoDao.getAllPhotos().first()
                photos.forEach { photo ->
                    val file = File(photo.filePath)
                    if (file.exists()) {
                        images[photo.filePath] = file
                    }
                }
            }

            writeKKbZip(outputUri, manifest, database, images)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readPreview(uri: Uri): ImportPreview? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "import_temp.kkb")
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val zipFile = ZipFile(tempFile)
            val manifestEntry = zipFile.getEntry("manifest.json") ?: return null
            val databaseEntry = zipFile.getEntry("database.json") ?: return null

            val manifestText = zipFile.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
            val databaseText = zipFile.getInputStream(databaseEntry).bufferedReader().use { it.readText() }
            zipFile.close()

            val manifest = JSONObject(manifestText)
            val database = JSONObject(databaseText)
            val contains = manifest.optJSONObject("contains") ?: JSONObject()

            val exportDateMs = manifest.optLong("exportDate", 0)
            val dateStr = if (exportDateMs > 0) {
                java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMANY)
                    .format(java.util.Date(exportDateMs))
            } else "Unbekannt"

            ImportPreview(
                manifest = manifest,
                knowledgeCount = contains.optInt("knowledge", 0),
                errorsCount = contains.optInt("errors", 0),
                errorCausesCount = contains.optInt("errorCauses", 0),
                projectsCount = contains.optInt("projects", 0),
                tasksCount = contains.optInt("tasks", 0),
                stepsCount = contains.optInt("steps", 0),
                skillsCount = contains.optInt("skills", 0),
                achievementsCount = contains.optInt("achievements", 0),
                templatesCount = contains.optInt("templates", 0),
                photosCount = contains.optInt("photos", 0),
                checklistsCount = contains.optInt("checklists", 0),
                checklistItemsCount = contains.optInt("checklistItems", 0),
                projectAssetsCount = contains.optInt("projectAssets", 0),
                exportDate = dateStr,
                appVersion = manifest.optString("version", "?")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importKKb(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val tempFile = File(context.cacheDir, "import_temp.kkb")
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val zipFile = ZipFile(tempFile)
            val databaseEntry = zipFile.getEntry("database.json") ?: return false
            val databaseText = zipFile.getInputStream(databaseEntry).bufferedReader().use { it.readText() }
            val database = JSONObject(databaseText)

            val taskMapping = mutableMapOf<String, String>()
            val projectMapping = mutableMapOf<String, String>()
            val checklistMapping = mutableMapOf<String, String>()

            if (database.has("projects")) {
                val arr = database.getJSONArray("projects")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldId = obj.getString("id")
                    val newId = UUID.randomUUID().toString()
                    projectMapping[oldId] = newId
                    projectDao.insertProject(ProjectEntity(
                        id = newId,
                        name = obj.optString("name"),
                        address = obj.optString("address"),
                        contactName = obj.optString("contactName"),
                        contactPhone = obj.optString("contactPhone"),
                        createdAt = obj.optLong("createdAt")
                    ))
                }
            }

            if (database.has("tasks")) {
                val arr = database.getJSONArray("tasks")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldId = obj.getString("id")
                    val newId = UUID.randomUUID().toString()
                    taskMapping[oldId] = newId
                    val oldProjectId = obj.optNullableString("projectId")
                    val newProjectId = oldProjectId?.let { projectMapping[it] }
                    workTaskDao.insertTask(WorkTaskEntity(
                        id = newId,
                        title = obj.optString("title"),
                        category = obj.optString("category"),
                        description = obj.optString("description"),
                        location = obj.optString("location"),
                        customerName = obj.optString("customerName"),
                        date = obj.optLong("date"),
                        isCompleted = obj.optBoolean("isCompleted"),
                        projectId = newProjectId,
                        durationSeconds = obj.optInt("durationSeconds"),
                        notes = obj.optString("notes"),
                        voiceNotePath = obj.optNullableString("voiceNotePath"),
                        createdAt = obj.optLong("createdAt"),
                        updatedAt = obj.optLong("updatedAt"),
                        materials = obj.optString("materials"),
                        tools = obj.optString("tools"),
                        solution = obj.optString("solution"),
                        rating = obj.optInt("rating")
                    ))
                }
            }

            if (database.has("steps")) {
                val arr = database.getJSONArray("steps")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldTaskId = obj.getString("taskId")
                    val newTaskId = taskMapping[oldTaskId] ?: oldTaskId
                    workStepDao.insertStep(WorkStepEntity(
                        id = UUID.randomUUID().toString(),
                        taskId = newTaskId,
                        stepOrder = obj.optInt("stepOrder"),
                        description = obj.optString("description"),
                        isDone = obj.optBoolean("isDone"),
                        warning = obj.optNullableString("warning"),
                        imagePath = null,
                        durationSeconds = obj.optInt("durationSeconds")
                    ))
                }
            }

            if (database.has("errors")) {
                val arr = database.getJSONArray("errors")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldTaskId = obj.optNullableString("taskId")
                    val newTaskId = oldTaskId?.let { taskMapping[it] }
                    errorLogDao.insertError(ErrorLogEntity(
                        id = UUID.randomUUID().toString(),
                        taskCategory = obj.optString("taskCategory"),
                        description = obj.optString("description"),
                        severity = obj.optInt("severity"),
                        date = obj.optLong("date"),
                        taskId = newTaskId,
                        wasAvoided = obj.optBoolean("wasAvoided"),
                        causes = obj.optString("causes"),
                        causeIds = obj.optString("causeIds"),
                        solution = obj.optString("solution")
                    ))
                }
            }

            if (database.has("errorCauses")) {
                val arr = database.getJSONArray("errorCauses")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    errorCauseDao.insertCause(ErrorCauseEntity(
                        id = UUID.randomUUID().toString(),
                        label = obj.optString("label"),
                        description = obj.optString("description"),
                        category = obj.optString("category")
                    ))
                }
            }

            if (database.has("knowledge")) {
                val arr = database.getJSONArray("knowledge")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    knowledgeBaseDao.insertEntry(KnowledgeBaseEntity(
                        id = UUID.randomUUID().toString(),
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
            }

            if (database.has("skills")) {
                val arr = database.getJSONArray("skills")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    skillDao.insertSkill(SkillEntity(
                        id = UUID.randomUUID().toString(),
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        description = obj.optString("description"),
                        iconName = obj.optString("iconName"),
                        currentXp = obj.optInt("currentXp"),
                        level = obj.optInt("level"),
                        maxLevel = obj.optInt("maxLevel"),
                        nextLevelXp = obj.optInt("nextLevelXp")
                    ))
                }
            }

            if (database.has("achievements")) {
                val arr = database.getJSONArray("achievements")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    achievementDao.insertAll(listOf(AchievementEntity(
                        id = UUID.randomUUID().toString(),
                        name = obj.optString("name"),
                        description = obj.optString("description"),
                        tier = obj.optInt("tier"),
                        isUnlocked = obj.optBoolean("isUnlocked"),
                        unlockedAt = if (obj.isNull("unlockedAt")) null else obj.optLong("unlockedAt"),
                        isBuiltIn = false
                    )))
                }
            }

            if (database.has("templates")) {
                val arr = database.getJSONArray("templates")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    workflowTemplateDao.insertTemplate(WorkflowTemplateEntity(
                        id = UUID.randomUUID().toString(),
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        stepsJson = obj.optString("stepsJson"),
                        isBuiltIn = obj.optBoolean("isBuiltIn"),
                        usageCount = obj.optInt("usageCount"),
                        createdAt = obj.optLong("createdAt")
                    ))
                }
            }

            if (database.has("checklists")) {
                val arr = database.getJSONArray("checklists")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldId = obj.getString("id")
                    val newId = UUID.randomUUID().toString()
                    checklistMapping[oldId] = newId
                    checklistDao.insertChecklist(ChecklistEntity(
                        id = newId,
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        createdAt = obj.optLong("createdAt"),
                        updatedAt = obj.optLong("updatedAt")
                    ))
                }
            }

            if (database.has("checklistItems")) {
                val arr = database.getJSONArray("checklistItems")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldChecklistId = obj.getString("checklistId")
                    val newChecklistId = checklistMapping[oldChecklistId] ?: oldChecklistId
                    checklistDao.insertItem(ChecklistItemEntity(
                        id = UUID.randomUUID().toString(),
                        checklistId = newChecklistId,
                        text = obj.optString("text"),
                        isChecked = obj.optBoolean("isChecked"),
                        orderIndex = obj.optInt("orderIndex")
                    ))
                }
            }

            if (database.has("projectAssets")) {
                val arr = database.getJSONArray("projectAssets")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldProjectId = obj.getString("projectId")
                    val newProjectId = projectMapping[oldProjectId] ?: oldProjectId
                    projectAssetDao.insertAsset(ProjectAssetEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = newProjectId,
                        type = obj.optString("type"),
                        title = obj.optString("title"),
                        location = obj.optString("location"),
                        fuseType = obj.optString("fuseType"),
                        circuitInfo = obj.optString("circuitInfo"),
                        uvType = obj.optString("uvType"),
                        description = obj.optString("description"),
                        notes = obj.optString("notes"),
                        photoPaths = obj.optString("photoPaths"),
                        docPaths = obj.optString("docPaths"),
                        createdAt = obj.optLong("createdAt")
                    ))
                }
            }

            if (database.has("photos")) {
                val imagesDir = File(context.filesDir, "imported_images")
                imagesDir.mkdirs()

                val arr = database.getJSONArray("photos")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val oldFilePath = obj.optString("filePath")
                    val newFilePath = try {
                        val entry = zipFile.getEntry("images/${File(oldFilePath).name}")
                        if (entry != null) {
                            val outputFile = File(imagesDir, File(oldFilePath).name)
                            zipFile.getInputStream(entry).use { input ->
                                outputFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            outputFile.absolutePath
                        } else oldFilePath
                    } catch (e: Exception) { oldFilePath }

                    photoDao.insertPhoto(PhotoEntity(
                        id = UUID.randomUUID().toString(),
                        taskId = obj.optNullableString("taskId")?.let { taskMapping[it] },
                        stepId = obj.optNullableString("stepId"),
                        filePath = newFilePath,
                        caption = obj.optString("caption"),
                        createdAt = obj.optLong("createdAt")
                    ))
                }
            }

            zipFile.close()
            tempFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeKKbZip(uri: Uri, manifest: JSONObject, database: JSONObject, images: Map<String, File>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString(2).toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("database.json"))
                zip.write(database.toString(2).toByteArray())
                zip.closeEntry()

                images.forEach { (originalPath, file) ->
                    val entryName = "images/${file.name}"
                    zip.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }

    private fun <T> toJsonArray(items: List<T>, transform: (T) -> JSONObject): JSONArray {
        val arr = JSONArray()
        items.forEach { arr.put(transform(it)) }
        return arr
    }

    /**
     * org.json.optString(key, null) liefert bei JSON-NULL den Literal-String "null".
     * Safer Lesehelfer: vorhandenes JSON-NULL oder fehlender Key -> Kotlin null.
     */
    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key)

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

    private fun ErrorCauseEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("description", description)
        put("category", category)
    }

    private fun ProjectEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("address", address)
        put("contactName", contactName)
        put("contactPhone", contactPhone)
        put("createdAt", createdAt)
    }

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

    private fun WorkStepEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("taskId", taskId)
        put("stepOrder", stepOrder)
        put("description", description)
        put("isDone", isDone)
        put("warning", warning ?: JSONObject.NULL)
        put("durationSeconds", durationSeconds)
    }

    private fun SkillEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("description", description)
        put("iconName", iconName)
        put("currentXp", currentXp)
        put("level", level)
        put("maxLevel", maxLevel)
        put("nextLevelXp", nextLevelXp)
    }

    private fun AchievementEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description)
        put("tier", tier)
        put("isUnlocked", isUnlocked)
        put("unlockedAt", unlockedAt ?: JSONObject.NULL)
    }

    private fun WorkflowTemplateEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("stepsJson", stepsJson)
        put("isBuiltIn", isBuiltIn)
        put("usageCount", usageCount)
        put("createdAt", createdAt)
    }

    private fun PhotoEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("taskId", taskId ?: JSONObject.NULL)
        put("stepId", stepId ?: JSONObject.NULL)
        put("filePath", filePath)
        put("caption", caption)
        put("createdAt", createdAt)
    }

    private fun ChecklistEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun ChecklistItemEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("checklistId", checklistId)
        put("text", text)
        put("isChecked", isChecked)
        put("orderIndex", orderIndex)
    }

    private fun ProjectAssetEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("projectId", projectId)
        put("type", type)
        put("title", title)
        put("location", location)
        put("fuseType", fuseType)
        put("circuitInfo", circuitInfo)
        put("uvType", uvType)
        put("description", description)
        put("notes", notes)
        put("photoPaths", photoPaths)
        put("docPaths", docPaths)
        put("createdAt", createdAt)
    }
}
