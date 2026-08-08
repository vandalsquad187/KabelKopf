package com.docai.kabelkopf.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin
    private var yPos = margin

    fun exportTaskPdf(task: WorkTaskEntity, steps: List<WorkStepEntity>): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            yPos = margin
            drawHeader(canvas, "Arbeitsbericht")
            drawField(canvas, "Titel", task.title)
            drawField(canvas, "Kategorie", task.category)
            if (task.location.isNotBlank()) drawField(canvas, "Ort", task.location)
            if (task.customerName.isNotBlank()) drawField(canvas, "Kunde", task.customerName)
            if (task.description.isNotBlank()) drawField(canvas, "Beschreibung", task.description)
            if (task.materials.isNotBlank()) drawField(canvas, "Materialien", task.materials)
            if (task.tools.isNotBlank()) drawField(canvas, "Werkzeug", task.tools)
            if (task.rating > 0) {
                val ratingLabel = when {
                    task.rating <= 2 -> "${task.rating} Sterne - Mangelhaft"
                    task.rating == 3 -> "${task.rating} Sterne - Befriedigend"
                    task.rating == 4 -> "${task.rating} Sterne - Gut"
                    else -> "${task.rating} Sterne - Sehr gut"
                }
                drawField(canvas, "Bewertung", ratingLabel)
            }

            if (steps.isNotEmpty()) {
                drawHeader(canvas, "Arbeitsschritte")
                steps.forEachIndexed { index, step ->
                    val status = if (step.isDone) "[✓]" else "[ ]"
                    drawWrappedText(canvas, "$status ${index + 1}. ${step.description}")
                    if (!step.warning.isNullOrBlank()) {
                        drawWrappedText(canvas, "   ⚠ ${step.warning}")
                    }
                }
            }

            drawField(canvas, "Erstellt am",
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date(task.date)))

            document.finishPage(page)

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(Date())
            val file = File(context.cacheDir, "Arbeit_${task.title.take(20)}_$dateStr.pdf")
            document.writeTo(file.outputStream())
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "PDF teilen"))
    }

    private fun drawHeader(canvas: Canvas, text: String) {
        val paint = TextPaint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textSize = 18f * context.resources.displayMetrics.density
            typeface = Typeface.DEFAULT_BOLD
        }
        yPos += 16f
        canvas.drawText(text, margin, yPos, paint)
        yPos += 8f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            strokeWidth = 1.5f
        })
        yPos += 12f
    }

    private fun drawField(canvas: Canvas, label: String, value: String) {
        val labelPaint = TextPaint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f * context.resources.displayMetrics.density
            typeface = Typeface.DEFAULT_BOLD
        }
        val valuePaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f * context.resources.displayMetrics.density
        }

        canvas.drawText(label, margin, yPos, labelPaint)
        yPos += 14f

        val layout = StaticLayout.Builder.obtain(value, 0, value.length, valuePaint, contentWidth.toInt())
            .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .build()
        canvas.save()
        canvas.translate(margin, yPos)
        layout.draw(canvas)
        canvas.restore()
        yPos += layout.height + 8f
    }

    private fun drawWrappedText(canvas: Canvas, text: String) {
        val paint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f * context.resources.displayMetrics.density
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth.toInt())
            .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(3f, 1f)
            .build()
        canvas.save()
        canvas.translate(margin + 8f, yPos)
        layout.draw(canvas)
        canvas.restore()
        yPos += layout.height + 6f
    }
}
