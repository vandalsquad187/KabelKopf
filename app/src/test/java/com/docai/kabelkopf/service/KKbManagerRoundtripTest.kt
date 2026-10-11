package com.docai.kabelkopf.service

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.docai.kabelkopf.data.local.AppDatabase
import com.docai.kabelkopf.data.local.entity.AchievementEntity
import com.docai.kabelkopf.data.local.entity.ChecklistEntity
import com.docai.kabelkopf.data.local.entity.ChecklistItemEntity
import com.docai.kabelkopf.data.local.entity.ErrorCauseEntity
import com.docai.kabelkopf.data.local.entity.ErrorLogEntity
import com.docai.kabelkopf.data.local.entity.KnowledgeBaseEntity
import com.docai.kabelkopf.data.local.entity.PhotoEntity
import com.docai.kabelkopf.data.local.entity.ProjectAssetEntity
import com.docai.kabelkopf.data.local.entity.ProjectEntity
import com.docai.kabelkopf.data.local.entity.SkillEntity
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.local.entity.WorkflowTemplateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Roundtrip-Test für das .kkb-Format (Datenrettungs-Pfad):
 * Export aus DB A -> Preview lesen -> Import in leere DB B.
 *
 * Prüft nicht nur Zählwerk, sondern vor allem die FK-Mapping-Logik:
 * Import generiert neue UUIDs, Fremdschlüssel (task.projectId, step.taskId,
 * checklistItem.checklistId, asset.projectId, error.taskId, photo.taskId)
 * müssen auf die NEUEN IDs zeigen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KKbManagerRoundtripTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var exportDb: AppDatabase
    private lateinit var importDb: AppDatabase
    private lateinit var exportManager: KKbManager
    private lateinit var importManager: KKbManager
    private lateinit var exportFile: File
    private lateinit var photoFile: File

    @Before
    fun setUp() {
        exportDb = openDb()
        importDb = openDb()
        exportManager = buildManager(exportDb)
        importManager = buildManager(importDb)
        exportFile = File(context.cacheDir, "roundtrip_export.kkb")
        if (exportFile.exists()) exportFile.delete()

        photoFile = File(context.cacheDir, "photo12345.jpg")
        photoFile.writeText("FAKEJPEG")
    }

    @After
    fun tearDown() {
        exportDb.close()
        importDb.close()
        exportFile.delete()
        photoFile.delete()
        File(context.filesDir, "imported_images").deleteRecursively()
    }

    private fun openDb(): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private fun buildManager(db: AppDatabase) = KKbManager(
        context,
        db.workTaskDao(),
        db.workStepDao(),
        db.errorLogDao(),
        db.errorCauseDao(),
        db.knowledgeBaseDao(),
        db.projectDao(),
        db.skillDao(),
        db.achievementDao(),
        db.workflowTemplateDao(),
        db.photoDao(),
        db.checklistDao(),
        db.projectAssetDao()
    )

    private suspend fun seedSourceData() {
        exportDb.projectDao().insertProject(ProjectEntity(
            id = "proj-1",
            name = "EFH Musterstraße",
            address = "Musterstraße 123",
            contactName = "Herr Müller",
            contactPhone = "+49 170 1234567",
            createdAt = 1L
        ))
        exportDb.workTaskDao().insertTask(WorkTaskEntity(
            id = "task-1",
            projectId = "proj-1",
            customerName = "Müller",
            title = "UV verdrahtet",
            category = "UV",
            description = "3-phasig verkabelt",
            location = "Keller",
            date = 2L,
            durationSeconds = 456,
            notes = "",
            voiceNotePath = null,
            createdAt = 2L,
            updatedAt = 2L,
            isCompleted = false
        ))
        exportDb.workStepDao().insertStep(WorkStepEntity(
            id = "step-1", taskId = "task-1", stepOrder = 0,
            description = "Hauptsicherung raus", isDone = true,
            warning = null, imagePath = null, durationSeconds = 0
        ))
        exportDb.workStepDao().insertStep(WorkStepEntity(
            id = "step-2", taskId = "task-1", stepOrder = 1,
            description = "Klemmen beschriften", isDone = false,
            warning = "Spannung prüfen!", imagePath = null, durationSeconds = 0
        ))
        exportDb.knowledgeBaseDao().insertEntry(KnowledgeBaseEntity(
            id = "kb-1", title = "Phasenreihenfolge", content = "L1 L2 L3 = braun schwarz grau",
            tags = "L1,L2,L3", category = "UV", isFavorite = true,
            sourceTaskId = null, createdAt = 3L, updatedAt = 3L
        ))
        exportDb.checklistDao().insertChecklist(ChecklistEntity(
            id = "cl-1", name = "UV-Abnahme", category = "UV", createdAt = 4L, updatedAt = 4L
        ))
        exportDb.checklistDao().insertItem(ChecklistItemEntity(
            id = "cli-1", checklistId = "cl-1", text = "Sicherungen beschriftet",
            isChecked = true, orderIndex = 0
        ))
        exportDb.checklistDao().insertItem(ChecklistItemEntity(
            id = "cli-2", checklistId = "cl-1", text = "Erdung gemessen",
            isChecked = false, orderIndex = 1
        ))
        exportDb.projectAssetDao().insertAsset(ProjectAssetEntity(
            id = "asset-1", projectId = "proj-1", type = "UV", title = "UV Keller",
            location = "Keller Nord", fuseType = "B16", circuitInfo = "3 Phasen",
            uvType = "Hager", description = "Neubau", notes = "",
            photoPaths = "", docPaths = "", createdAt = 5L
        ))
        exportDb.errorLogDao().insertError(ErrorLogEntity(
            id = "err-1", taskCategory = "UV", description = "N und PE vertauscht",
            severity = 5, date = 6L, taskId = "task-1", wasAvoided = false,
            causes = "", causeIds = "", solution = "Immer messen"
        ))
        exportDb.errorCauseDao().insertCause(ErrorCauseEntity(
            id = "cause-1", label = "Verkabelungsfehler", description = "", category = "UV"
        ))
        exportDb.photoDao().insertPhoto(PhotoEntity(
            id = "photo-1", taskId = "task-1", stepId = null,
            filePath = photoFile.absolutePath, caption = "UV-Foto", createdAt = 7L
        ))
        exportDb.skillDao().insertSkill(SkillEntity(
            id = "skill-1", name = "UV-Montage", category = "UV", description = "",
            iconName = "bolt", currentXp = 20, level = 1, maxLevel = 10, nextLevelXp = 100
        ))
        exportDb.achievementDao().insertAll(listOf(AchievementEntity(
            id = "ach-1", name = "Erste Arbeit", description = "", tier = 1,
            isUnlocked = true, unlockedAt = 8L, isBuiltIn = true
        )))
        exportDb.workflowTemplateDao().insertTemplate(WorkflowTemplateEntity(
            id = "tpl-1", name = "UV-Montage", category = "UV",
            stepsJson = "Schritt A\n---\nSchritt B", isBuiltIn = true,
            usageCount = 2, createdAt = 9L
        ))
    }

    @Test
    fun roundtrip_preservesData_andRemapsForeignKeys() = runBlocking {
        seedSourceData()

        // 1. Export
        val exported = exportManager.exportKKb(ExportSelection(), Uri.fromFile(exportFile))
        assertTrue("Export muss erfolgreich sein", exported)
        assertTrue("Export-Datei muss existieren", exportFile.exists())
        assertTrue("Export-Datei darf nicht leer sein", exportFile.length() > 0)

        // 2. Preview muss Zählwerk korrekt lesen
        val preview = exportManager.readPreview(Uri.fromFile(exportFile))
        assertNotNull("Preview muss lesbar sein", preview!!)
        assertEquals(1, preview.projectsCount)
        assertEquals(1, preview.tasksCount)
        assertEquals(2, preview.stepsCount)
        assertEquals(1, preview.knowledgeCount)
        assertEquals(1, preview.checklistsCount)
        assertEquals(2, preview.checklistItemsCount)
        assertEquals(1, preview.projectAssetsCount)
        assertEquals(1, preview.errorsCount)
        assertEquals(1, preview.errorCausesCount)
        assertEquals(1, preview.photosCount)
        assertEquals(1, preview.skillsCount)
        assertEquals(1, preview.achievementsCount)
        assertEquals(1, preview.templatesCount)

        // 3. Import in die LEERE DB B
        val imported = importManager.importKKb(Uri.fromFile(exportFile))
        assertTrue("Import muss erfolgreich sein", imported)

        // 4. FK-Mapping prüfen: neue IDs, korrekte Verkettung
        val projects = importDb.projectDao().getAllProjects().first()
        assertEquals(1, projects.size)
        val newProject = projects[0]
        assertNotEquals("Projekt muss neue UUID bekommen", "proj-1", newProject.id)
        assertEquals("EFH Musterstraße", newProject.name)
        assertEquals("+49 170 1234567", newProject.contactPhone)

        val tasks = importDb.workTaskDao().getAllTasks().first()
        assertEquals(1, tasks.size)
        val newTask = tasks[0]
        assertNotEquals("Task muss neue UUID bekommen", "task-1", newTask.id)
        assertEquals("Task.projectId muss auf importiertes Projekt zeigen",
            newProject.id, newTask.projectId)
        assertEquals("UV verdrahtet", newTask.title)
        assertEquals("Timer-Wert muss überleben", 456, newTask.durationSeconds)

        val steps = importDb.workStepDao().getAllSteps().first()
            .sortedBy { it.stepOrder }
        assertEquals(2, steps.size)
        assertTrue("Alle Steps müssen auf neuen Task zeigen",
            steps.all { it.taskId == newTask.id })
        assertEquals("Hauptsicherung raus", steps[0].description)
        assertTrue(steps[0].isDone)
        assertNull("Step ohne Warnung muss null bleiben (nicht 'null'-String)",
            steps[0].warning)
        assertEquals("Klemmen beschriften", steps[1].description)
        assertEquals("Warnung muss überleben", "Spannung prüfen!", steps[1].warning)

        val assets = importDb.projectAssetDao().getAllAssets().first()
        assertEquals(1, assets.size)
        assertEquals("Asset muss auf importiertes Projekt zeigen",
            newProject.id, assets[0].projectId)
        assertEquals("Hager", assets[0].uvType)

        val errors = importDb.errorLogDao().getAllErrors().first()
        assertEquals(1, errors.size)
        assertEquals("Error muss auf importierten Task zeigen", newTask.id, errors[0].taskId)
        assertEquals(5, errors[0].severity)
        assertEquals("Immer messen", errors[0].solution)

        val causes = importDb.errorCauseDao().getAllCauses().first()
        assertEquals(1, causes.size)
        assertEquals("Verkabelungsfehler", causes[0].label)

        val knowledge = importDb.knowledgeBaseDao().getAllEntries().first()
        assertEquals(1, knowledge.size)
        assertNotEquals("kb-1", knowledge[0].id)
        assertEquals("Phasenreihenfolge", knowledge[0].title)
        assertTrue("Favoriten-Flag muss überleben", knowledge[0].isFavorite)

        val checklists = importDb.checklistDao().getAllChecklists().first()
        assertEquals(1, checklists.size)
        val newChecklist = checklists[0]
        assertNotEquals("cl-1", newChecklist.id)

        val items = importDb.checklistDao().getAllChecklistItems().first()
            .sortedBy { it.orderIndex }
        assertEquals(2, items.size)
        assertTrue("Alle Items müssen auf importierte Checklist zeigen",
            items.all { it.checklistId == newChecklist.id })
        assertEquals("Sicherungen beschriftet", items[0].text)
        assertTrue("Häkchen muss überleben", items[0].isChecked)
        assertEquals("Erdung gemessen", items[1].text)

        // 5. Foto: Datei muss ins imported_images-Verzeichnis kopiert sein
        val photos = importDb.photoDao().getAllPhotos().first()
        assertEquals(1, photos.size)
        val newPhoto = photos[0]
        assertNotEquals("photo-1", newPhoto.id)
        assertEquals("Photo muss auf importierten Task zeigen", newTask.id, newPhoto.taskId)
        assertTrue("Pfad muss ins imported_images-Verzeichnis zeigen",
            newPhoto.filePath.contains("imported_images"))
        val copiedPhoto = File(newPhoto.filePath)
        assertTrue("Foto-Datei muss kopiert sein", copiedPhoto.exists())
        assertEquals("Foto-Inhalt muss unverändert sein", "FAKEJPEG", copiedPhoto.readText())

        val skills = importDb.skillDao().getAllSkills().first()
        assertEquals(1, skills.size)
        assertEquals(20, skills[0].currentXp)

        val achievements = importDb.achievementDao().getAllAchievements().first()
        assertEquals(1, achievements.size)
        assertTrue(achievements[0].isUnlocked)

        val templates = importDb.workflowTemplateDao().getAllTemplates().first()
        assertEquals(1, templates.size)
        assertEquals("Schritt A\n---\nSchritt B", templates[0].stepsJson)
    }

    @Test
    fun export_withoutData_stillProducesValidArchive() = runBlocking {
        val exported = exportManager.exportKKb(ExportSelection(), Uri.fromFile(exportFile))
        assertTrue(exported)

        val preview = exportManager.readPreview(Uri.fromFile(exportFile))
        assertNotNull(preview!!)
        assertEquals(0, preview.tasksCount)
        assertEquals(0, preview.knowledgeCount)
    }

    @Test
    fun preview_withCorruptFile_returnsNull() {
        val corrupt = File(context.cacheDir, "corrupt.kkb")
        corrupt.writeText("kein zip")
        val preview = exportManager.readPreview(Uri.fromFile(corrupt))
        assertNull(preview)
        corrupt.delete()
    }

    @Test
    fun export_withSingleSelection_exportsOnlySelectedData() = runBlocking {
        seedSourceData()

        val selection = ExportSelection(
            knowledge = true,
            errors = false, errorCauses = false, projects = false, tasks = false,
            steps = false, skills = false, achievements = false, templates = false,
            photos = false, checklists = false, checklistItems = false, projectAssets = false
        )
        val exported = exportManager.exportKKb(selection, Uri.fromFile(exportFile))
        assertTrue(exported)

        val preview = exportManager.readPreview(Uri.fromFile(exportFile))
        assertNotNull(preview!!)
        assertEquals("Nur Knowledge darf im Export sein", 1, preview.knowledgeCount)
        assertEquals(0, preview.tasksCount)
        assertEquals(0, preview.projectsCount)
        assertEquals(0, preview.photosCount)
        assertFalse(File(context.cacheDir, "images").exists())
    }
}
