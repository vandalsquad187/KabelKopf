package com.docai.kabelkopf.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.docai.kabelkopf.data.local.entity.ProjectAssetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Migration-Smoke-Test 6 -> 7 (Roadmap Phase C).
 *
 * Baut eine v6-Datenbank manuell auf (alle Tabellen außer project_assets),
 * legt Testdaten an, und lässt Room dann über MIGRATION_6_7 auf v7 migrieren.
 * Prüft: (1) bestehende Daten überleben, (2) project_assets funktioniert,
 * (3) Room-Schema-Validierung akzeptiert das Ergebnis.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration6To7Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"
    private val dbFile get() = context.getDatabasePath(dbName)

    /** v6-Schema = v7-Schema ohne project_assets (aus app/schemas/.../7.json erzeugt). */
    private val v6Statements = listOf(
        "CREATE TABLE IF NOT EXISTS `work_tasks` (`id` TEXT NOT NULL, `projectId` TEXT, `customerName` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `description` TEXT NOT NULL, `location` TEXT NOT NULL, `date` INTEGER NOT NULL, `durationSeconds` INTEGER NOT NULL, `notes` TEXT NOT NULL, `voiceNotePath` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `materials` TEXT NOT NULL, `tools` TEXT NOT NULL, `solution` TEXT NOT NULL, `rating` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `work_steps` (`id` TEXT NOT NULL, `taskId` TEXT NOT NULL, `stepOrder` INTEGER NOT NULL, `description` TEXT NOT NULL, `isDone` INTEGER NOT NULL, `warning` TEXT, `imagePath` TEXT, `durationSeconds` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `error_logs` (`id` TEXT NOT NULL, `taskCategory` TEXT NOT NULL, `description` TEXT NOT NULL, `severity` INTEGER NOT NULL, `date` INTEGER NOT NULL, `taskId` TEXT, `wasAvoided` INTEGER NOT NULL, `causes` TEXT NOT NULL, `causeIds` TEXT NOT NULL, `solution` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `knowledge_base` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `tags` TEXT NOT NULL, `category` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `sourceTaskId` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `workflow_templates` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `stepsJson` TEXT NOT NULL, `isBuiltIn` INTEGER NOT NULL, `usageCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `photos` (`id` TEXT NOT NULL, `taskId` TEXT, `stepId` TEXT, `filePath` TEXT NOT NULL, `caption` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `projects` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `contactName` TEXT NOT NULL, `contactPhone` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `error_causes` (`id` TEXT NOT NULL, `label` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `skills` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `description` TEXT NOT NULL, `iconName` TEXT NOT NULL, `currentXp` INTEGER NOT NULL, `level` INTEGER NOT NULL, `maxLevel` INTEGER NOT NULL, `nextLevelXp` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconName` TEXT NOT NULL, `tier` INTEGER NOT NULL, `isUnlocked` INTEGER NOT NULL, `unlockedAt` INTEGER, `isBuiltIn` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `checklists` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        "CREATE TABLE IF NOT EXISTS `checklist_items` (`id` TEXT NOT NULL, `checklistId` TEXT NOT NULL, `text` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, `orderIndex` INTEGER NOT NULL, PRIMARY KEY(`id`))"
    )

    @Before
    fun setUp() {
        context.deleteDatabase(dbName)
        dbFile.parentFile?.mkdirs()

        // v6-Datenbank anlegen
        val v6 = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        v6Statements.forEach { v6.execSQL(it) }
        v6.execSQL(
            "INSERT INTO work_tasks (id, projectId, customerName, title, category, description, " +
                "location, date, durationSeconds, notes, voiceNotePath, createdAt, updatedAt, " +
                "isCompleted, materials, tools, solution, rating) " +
                "VALUES ('t1', NULL, 'Kunde', 'Alte Arbeit', 'UV', 'Beschreibung', 'Keller', " +
                "1234567890, 600, '', NULL, 1234567890, 1234567890, 0, '', '', '', 0)"
        )
        v6.execSQL(
            "INSERT INTO knowledge_base (id, title, content, tags, category, isFavorite, " +
                "sourceTaskId, createdAt, updatedAt) " +
                "VALUES ('k1', 'Wissen', 'Inhalt', 'tag', 'UV', 1, NULL, 1, 1)"
        )
        v6.version = 6
        v6.close()
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migration6To7_preservesData_andCreatesProjectAssets() = runBlocking {
        // Room öffnet v6-DB -> MIGRATION_6_7 muss greifen
        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_6_7)
            .allowMainThreadQueries()
            .build()
        try {
            // Bestehende Daten unverändert
            val tasks = db.workTaskDao().getAllTasks().first()
            assertEquals(1, tasks.size)
            val task = tasks[0]
            assertEquals("Alte Arbeit", task.title)
            assertEquals("Kunde", task.customerName)
            assertEquals(600, task.durationSeconds)
            assertEquals(1234567890L, task.date)
            assertTrue(task.voiceNotePath == null)

            val knowledge = db.knowledgeBaseDao().getAllEntries().first()
            assertEquals(1, knowledge.size)
            assertEquals("Wissen", knowledge[0].title)
            assertTrue(knowledge[0].isFavorite)

            // Neue Tabelle ist beschreibbar und lesbar
            db.projectAssetDao().insertAsset(ProjectAssetEntity(
                id = "a1", projectId = "p1", type = "UV", title = "UV Keller",
                location = "Nord", fuseType = "B16", circuitInfo = "", uvType = "Hager",
                description = "", notes = "", photoPaths = "", docPaths = "", createdAt = 99L
            ))
            val assets = db.projectAssetDao().getAllAssets().first()
            assertEquals(1, assets.size)
            assertEquals("UV Keller", assets[0].title)
            assertEquals(99L, assets[0].createdAt)

            // Alte Daten weiterhin da, nachdem neue Tabelle genutzt wurde
            assertEquals(1, db.workTaskDao().getAllTasks().first().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun migration6To7_dbVersionIs7_afterMigration() = runBlocking {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_6_7)
            .allowMainThreadQueries()
            .build()
        try {
            // Erst collecten, damit Room die DB wirklich öffnet und die Migration ausführt
            db.workTaskDao().getAllTasks().first()
            val raw = SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
            )
            assertEquals("Datenbank muss auf Version 7 sein", 7, raw.version)
            raw.close()
        } finally {
            db.close()
        }
        assertTrue(File(dbFile.path).exists())
    }
}
