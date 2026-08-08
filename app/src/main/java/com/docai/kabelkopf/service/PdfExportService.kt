package com.docai.kabelkopf.service

import android.content.Context
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.util.DateUtils
import com.docai.kabelkopf.util.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateReport(
        task: WorkTaskEntity,
        steps: List<WorkStepEntity>
    ): File {
        val reportFile = File(FileUtils.getPdfDir(context), "report_${task.id}.txt")
        val writer = FileWriter(reportFile)

        writer.write("========================================\n")
        writer.write("         ARBEITSBERICHT\n")
        writer.write("========================================\n\n")
        writer.write("Titel: ${task.title}\n")
        writer.write("Kategorie: ${task.category}\n")
        if (task.location.isNotBlank()) writer.write("Ort: ${task.location}\n")
        if (task.customerName.isNotBlank()) writer.write("Kunde: ${task.customerName}\n")
        writer.write("Datum: ${DateUtils.formatDateTime(task.date)}\n\n")

        if (task.description.isNotBlank()) {
            writer.write("Beschreibung:\n${task.description}\n\n")
        }

        if (steps.isNotEmpty()) {
            writer.write("Arbeitsschritte:\n")
            writer.write(String.format("%-4s %-50s %-10s %s\n", "Nr.", "Schritt", "Status", "Warnung"))
            writer.write("-".repeat(80) + "\n")
            steps.forEach { step ->
                val status = if (step.isDone) "Erledigt" else "Offen"
                writer.write(String.format("%-4d %-50s %-10s %s\n",
                    step.stepOrder + 1,
                    step.description.take(50),
                    status,
                    step.warning ?: ""))
            }
        }

        writer.write("\n----------------------------------------\n")
        writer.write("Erstellt am: ${DateUtils.formatDateTime(System.currentTimeMillis())}\n")
        writer.close()
        return reportFile
    }
}
