package com.docai.kabelkopf.ui.screens.task

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.docai.kabelkopf.data.local.AppDatabase
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.AchievementRepository
import com.docai.kabelkopf.data.repository.ErrorCauseRepository
import com.docai.kabelkopf.data.repository.ErrorLogRepository
import com.docai.kabelkopf.data.repository.KnowledgeRepository
import com.docai.kabelkopf.data.repository.SkillRepository
import com.docai.kabelkopf.data.repository.StepRepository
import com.docai.kabelkopf.data.repository.TaskRepository
import com.docai.kabelkopf.data.repository.TemplateRepository
import com.docai.kabelkopf.service.GamificationManager
import com.docai.kabelkopf.service.PdfExportManager
import com.docai.kabelkopf.service.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression-Tests für den Stoppuhr-Save-Bug (fix in 1.0.8):
 * Dauer muss in allen drei Pfaden (saveTask, startEdit, confirmEdit)
 * korrekt zwischen UiState und Entity hin- und herwandern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskViewModelTimerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDatabase
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = TaskViewModel(
            taskRepository = TaskRepository(db.workTaskDao()),
            stepRepository = StepRepository(db.workStepDao()),
            errorLogRepository = ErrorLogRepository(db.errorLogDao()),
            templateRepository = TemplateRepository(db.workflowTemplateDao()),
            voiceRecorder = VoiceRecorder(context),
            errorCauseRepository = ErrorCauseRepository(db.errorCauseDao()),
            skillRepository = SkillRepository(db.skillDao()),
            knowledgeRepository = KnowledgeRepository(db.knowledgeBaseDao()),
            gamificationManager = GamificationManager(
                AchievementRepository(db.achievementDao()),
                db.workTaskDao(),
                db.errorLogDao(),
                db.skillDao(),
                db.projectDao(),
                db.workStepDao()
            ),
            pdfExportManager = PdfExportManager(context)
        )
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun saveTask_persistsTimerElapsedSeconds_asDuration() = runBlocking {
        viewModel.updateTitle("Testarbeit")
        repeat(7) { viewModel.tickTimer() }
        assertEquals(7, viewModel.state.value.timerElapsedSeconds)

        viewModel.saveTask()
        val savedId = viewModel.state.first { it.savedTaskId != null }.savedTaskId!!

        val saved = db.workTaskDao().getTaskById(savedId)
        assertEquals("Timer-Dauer muss in der DB landen", 7, saved!!.durationSeconds)
        assertFalse("Timer muss nach dem Speichern gestoppt sein",
            viewModel.state.value.isTimerRunning)
    }

    @Test
    fun startEdit_copiesDurationSeconds_intoUiState() = runBlocking {
        val taskId = seedTask(durationSeconds = 300)

        viewModel.loadTask(taskId)
        viewModel.task.first { it != null } // warten bis async load fertig
        viewModel.startEdit()

        assertEquals("Edit-Modus muss Dauer aus der DB laden", 300,
            viewModel.state.value.timerElapsedSeconds)
        assertTrue(viewModel.isEditing.value)
    }

    @Test
    fun confirmEdit_persistsEditedDurationSeconds() = runBlocking {
        val taskId = seedTask(durationSeconds = 300)

        viewModel.loadTask(taskId)
        viewModel.task.first { it != null } // warten bis async load fertig
        viewModel.startEdit()
        repeat(2) { viewModel.tickTimer() }
        viewModel.confirmEdit()
        viewModel.isEditing.first { !it }

        val updated = db.workTaskDao().getTaskById(taskId)
        assertEquals("Bearbeitete Dauer muss gespeichert werden", 302,
            updated!!.durationSeconds)
    }

    @Test
    fun cancelEdit_doesNotPersistChanges() = runBlocking {
        val taskId = seedTask(durationSeconds = 100)

        viewModel.loadTask(taskId)
        viewModel.startEdit()
        repeat(50) { viewModel.tickTimer() }
        viewModel.cancelEdit()

        val unchanged = db.workTaskDao().getTaskById(taskId)
        assertEquals("Abbrechen darf Dauer nicht ändern", 100, unchanged!!.durationSeconds)
    }

    @Test
    fun resetTimer_resetsElapsedToZero_andStops() = runBlocking {
        viewModel.updateTitle("Timer")
        viewModel.startTimer()
        repeat(5) { viewModel.tickTimer() }
        viewModel.resetTimer()

        assertEquals(0, viewModel.state.value.timerElapsedSeconds)
        assertFalse(viewModel.state.value.isTimerRunning)
    }

    private suspend fun seedTask(durationSeconds: Int): String {
        val task = WorkTaskEntity(
            id = java.util.UUID.randomUUID().toString(),
            projectId = null,
            customerName = "",
            title = "Bestehende Arbeit",
            category = "UV",
            description = "",
            location = "",
            date = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            notes = "",
            voiceNotePath = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isCompleted = false
        )
        db.workTaskDao().insertTask(task)
        return task.id
    }
}
