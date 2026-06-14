package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.EmotionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    /**
     * Generate a real PDF report summarizing the analytics history.
     * Draws beautifully styled card sections, margins, and data grids on the native canvas.
     */
    fun generatePdfReport(context: Context, records: List<EmotionRecord>): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- Styles & Paints ---
        val titlePaint = Paint().apply {
            color = Color.rgb(33, 150, 243) // Modern primary blue
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(44, 62, 80) // Dark Slate
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val accentLinePaint = Paint().apply {
            color = Color.rgb(33, 150, 243)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        var y = 40f

        // --- Draw Header ---
        canvas.drawText("EMOTIONAI PRO - DISCOVERY SUMMARIES", 50f, y, titlePaint)
        y += 12f
        canvas.drawLines(floatArrayOf(50f, y, 545f, y), accentLinePaint)
        y += 18f

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        canvas.drawText("SaaS Enterprise Health Analytics Report | Generated: $dateStr", 50f, y, subTitlePaint)
        y += 30f

        // --- Overview stats ---
        canvas.drawText("HEALTH REPORT OVERVIEW & SUMMARY", 50f, y, headerPaint)
        y += 6f
        canvas.drawLines(floatArrayOf(50f, y, 545f, y), linePaint)
        y += 18f

        val totalLogs = records.size
        val avgWellness = if (records.isNotEmpty()) records.map { it.wellnessScore }.average().toInt() else 0
        var happiestCount = 0
        var criticalStressCount = 0

        records.forEach {
            if (it.dominantEmotion.lowercase() == "happy" || it.dominantEmotion.lowercase() == "love") happiestCount++
            if (it.stressLevelScore > 0.6f || it.anxietyScore > 0.6f) criticalStressCount++
        }

        canvas.drawText("Total Analytical Tracks: $totalLogs", 60f, y, boldBodyPaint)
        canvas.drawText("Average Well-being Score: $avgWellness/100", 300f, y, boldBodyPaint)
        y += 18f
        canvas.drawText("Happiest Tracks: $happiestCount", 60f, y, bodyPaint)
        canvas.drawText("Critical Stress/Anxiety Levels Registered: $criticalStressCount", 300f, y, bodyPaint)
        y += 35f

        // --- Table Headers ---
        canvas.drawText("LATEST SPECIFIED EVENT RECORDS", 50f, y, headerPaint)
        y += 6f
        canvas.drawLines(floatArrayOf(50f, y, 545f, y), linePaint)
        y += 18f

        canvas.drawText("Date/Time", 50f, y, boldBodyPaint)
        canvas.drawText("Dominant Mood", 180f, y, boldBodyPaint)
        canvas.drawText("Conf %", 290f, y, boldBodyPaint)
        canvas.drawText("Well-Being Score", 360f, y, boldBodyPaint)
        canvas.drawText("Core Input Sample", 460f, y, boldBodyPaint)
        y += 6f
        canvas.drawLines(floatArrayOf(50f, y, 545f, y), linePaint)
        y += 14f

        // --- Table Rows ---
        val recordsToDraw = records.take(12) // Fit on single page nicely
        val tableDateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

        if (recordsToDraw.isEmpty()) {
            canvas.drawText("No tracked events on directory record.", 50f, y, bodyPaint)
        } else {
            recordsToDraw.forEach { record ->
                if (y > 760f) return@forEach // Stay inside page bounds
                val formattedTime = tableDateFormat.format(Date(record.timestamp))
                val inputSummary = if (record.inputText.length > 18) record.inputText.take(15) + "..." else record.inputText

                canvas.drawText(formattedTime, 50f, y, bodyPaint)
                canvas.drawText(record.dominantEmotion, 180f, y, bodyPaint)
                canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", record.confidenceScore * 100), 290f, y, bodyPaint)
                canvas.drawText(String.format(Locale.getDefault(), "%.0f/100", record.wellnessScore), 360f, y, bodyPaint)
                canvas.drawText(inputSummary, 460f, y, bodyPaint)
                y += 18f
            }
        }

        // --- Draw Footer ---
        y = 800f
        canvas.drawLines(floatArrayOf(50f, y, 545f, y), linePaint)
        y += 12f
        canvas.drawText("Confidential Report | EmotionAI Pro CRM SaaS platform | All health predictions are computer-assisted AI suggestions.", 50f, y, footerPaint)

        pdfDocument.finishPage(page)

        // --- Save the file ---
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) exportsDir.mkdirs()
        val pdfFile = File(exportsDir, "EmotionAI_Report_${System.currentTimeMillis()}.pdf")

        return try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            FileProvider.getUriForFile(context, "com.example.emotionaipro.fileprovider", pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Generate a real valid CSV file containing all tracked entries.
     * Includes logical escaping, headers, and floating metrics.
     */
    fun generateCsvReport(context: Context, records: List<EmotionRecord>): Uri? {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) exportsDir.mkdirs()
        val csvFile = File(exportsDir, "EmotionAI_Database_${System.currentTimeMillis()}.csv")

        val csvHeader = "ID,Timestamp,InputText,DominantEmotion,ConfidenceScore,WellnessScore,HappyScore,SadScore,AngryScore,FearScore,AnxietyScore,StressLevelScore\n"

        return try {
            val fos = FileOutputStream(csvFile)
            fos.write(csvHeader.toByteArray())

            records.forEach { r ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(r.timestamp))
                val escapedInput = r.inputText.replace("\"", "\"\"")
                
                val line = String.format(
                    Locale.US,
                    "%d,\"%s\",\"%s\",\"%s\",%.3f,%.1f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n",
                    r.id,
                    dateStr,
                    escapedInput,
                    r.dominantEmotion,
                    r.confidenceScore,
                    r.wellnessScore,
                    r.happyScore,
                    r.sadScore,
                    r.angryScore,
                    r.fearScore,
                    r.anxietyScore,
                    r.stressLevelScore
                )
                fos.write(line.toByteArray())
            }
            fos.close()
            FileProvider.getUriForFile(context, "com.example.emotionaipro.fileprovider", csvFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Triggers the Android sharesheet to send or save a targeted file.
     */
    fun shareExportedFile(context: Context, fileUri: Uri, mimeType: String, description: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, description))
    }
}
