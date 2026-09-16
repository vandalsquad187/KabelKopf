package com.docai.kabelkopf.service

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.docai.kabelkopf.BuildConfig
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

    fun exportTaskPdf(task: WorkTaskEntity, steps: List<WorkStepEntity>): File? {
        var document: PdfDocument? = null
        try {
            document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var yPos = margin
            yPos = drawHeader(canvas, yPos, "Arbeitsbericht")
            yPos = drawField(canvas, yPos, "Titel", task.title)
            yPos = drawField(canvas, yPos, "Kategorie", task.category)
            if (task.location.isNotBlank()) yPos = drawField(canvas, yPos, "Ort", task.location)
            if (task.customerName.isNotBlank()) yPos = drawField(canvas, yPos, "Kunde", task.customerName)
            if (task.description.isNotBlank()) yPos = drawField(canvas, yPos, "Beschreibung", task.description)
            if (task.materials.isNotBlank()) yPos = drawField(canvas, yPos, "Materialien", task.materials)
            if (task.tools.isNotBlank()) yPos = drawField(canvas, yPos, "Werkzeug", task.tools)
            if (task.rating > 0) {
                val ratingLabel = when {
                    task.rating <= 2 -> "${task.rating} Sterne - Mangelhaft"
                    task.rating == 3 -> "${task.rating} Sterne - Befriedigend"
                    task.rating == 4 -> "${task.rating} Sterne - Gut"
                    else -> "${task.rating} Sterne - Sehr gut"
                }
                yPos = drawField(canvas, yPos, "Bewertung", ratingLabel)
            }

            if (steps.isNotEmpty()) {
                yPos = drawHeader(canvas, yPos, "Arbeitsschritte")
                steps.forEachIndexed { index, step ->
                    val status = if (step.isDone) "[✓]" else "[ ]"
                    yPos = drawWrappedText(canvas, yPos, "$status ${index + 1}. ${step.description}")
                    if (!step.warning.isNullOrBlank()) {
                        yPos = drawWrappedText(canvas, yPos, "   ⚠ ${step.warning}")
                    }
                }
            }

            yPos = drawField(canvas, yPos, "Erstellt am",
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date(task.date)))

            document.finishPage(page)

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(Date())
            val safeTitle = task.title.take(20).replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val file = File(context.cacheDir, "Arbeit_${safeTitle}_$dateStr.pdf")
            file.parentFile?.mkdirs()
            file.outputStream().use { document.writeTo(it) }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            document?.close()
        }
    }

    fun sharePdf(file: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "PDF teilen"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF konnte nicht geteilt werden", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun drawHeader(canvas: Canvas, yPos: Float, text: String): Float {
        val paint = TextPaint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            textSize = 18f * context.resources.displayMetrics.density
            typeface = Typeface.DEFAULT_BOLD
        }
        var y = yPos + 16f
        canvas.drawText(text, margin, y, paint)
        y += 8f
        canvas.drawLine(margin, y, pageWidth - margin, y, Paint().apply {
            color = android.graphics.Color.parseColor("#1565C0")
            strokeWidth = 1.5f
        })
        y += 12f
        return y
    }

    private fun drawField(canvas: Canvas, yPos: Float, label: String, value: String): Float {
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
        var y = yPos + 14f

        val layout = StaticLayout.Builder.obtain(value, 0, value.length, valuePaint, contentWidth.toInt())
            .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .build()
        canvas.save()
        canvas.translate(margin, y)
        layout.draw(canvas)
        canvas.restore()
        y += layout.height + 8f
        return y
    }

    private fun drawWrappedText(canvas: Canvas, yPos: Float, text: String): Float {
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
        return yPos + layout.height + 6f
    }
}
