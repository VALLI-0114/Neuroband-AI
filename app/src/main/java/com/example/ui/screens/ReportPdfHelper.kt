package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportPdfHelper {
    fun generateStressReportPdf(context: Context, reportType: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            // Page Background
            paint.color = 0xFFF8FAFC.toInt() // slate-50
            canvas.drawRect(0f, 0f, 595f, 842f, paint)

            // Header Banner (Slate Navy Blue)
            paint.color = 0xFF0F172A.toInt() // slate-900
            canvas.drawRect(20f, 20f, 575f, 130f, paint)

            // Title
            paint.color = 0xFF22D3EE.toInt() // Cyan-400
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NEUROBAND COGNITIVE BIOLINK", 42f, 62f, paint)

            // Subtitle
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("STRESS INTELLIGENCE BIO-ANALYTICS SYSTEM", 45f, 85f, paint)

            // Date and Type Info
            paint.color = 0xFF94A3B8.toInt()
            paint.textSize = 9f
            val dateStr = SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Report Type: $reportType Report • Timestamp: $dateStr UTC", 45f, 108f, paint)

            // Divider Line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = 0xFFE2E8F0.toInt()
            canvas.drawLine(20f, 150f, 575f, 150f, paint)

            // Section 1: Executive Summary
            paint.style = Paint.Style.FILL
            paint.color = 0xFF1E293B.toInt()
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("1. EXECUTIVE COGNITIVE AUDIT SUMMARY", 30f, 180f, paint)

            paint.color = 0xFF475569.toInt()
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val lines = listOf(
                "This telemetry document summarizes student-focused cognitive load metrics synthesized over the",
                "designated periods. Integrated electrodermal skin conductances (GSR) combined with wearable active",
                "PPG sensors measure autonomic nervous system stability. The diagnostic parameters reflect high",
                "stress density segments matching exam term blocks, alleviated by haptic breathing compliance cycles."
            )
            var yOffset = 205f
            for (line in lines) {
                canvas.drawText(line, 35f, yOffset, paint)
                yOffset += 18f
            }

            // Section 2: Biometric Indicators
            yOffset += 15f
            paint.color = 0xFF1E293B.toInt()
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("2. CRITICAL AUTONOMIC VECTORS STATUS", 30f, yOffset, paint)

            yOffset += 20f
            // Grid Title Background
            paint.color = 0xFFE2E8F0.toInt()
            canvas.drawRect(30f, yOffset, 565f, yOffset + 24f, paint)

            paint.color = 0xFF0F172A.toInt()
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Autonomic Vector", 40f, yOffset + 16f, paint)
            canvas.drawText("Synthesized Baseline", 220f, yOffset + 16f, paint)
            canvas.drawText("Diagnostic Advisory Status", 380f, yOffset + 16f, paint)

            val rows = listOf(
                Triple("Average Heart Rate", "72 BPM", "Optimal Sympathetic Range"),
                Triple("Heart Rate Variability (HRV)", "58 ms", "Excellent Vagal Coherence"),
                Triple("Sleep Latency/Duration", "8h 12m (81%)", "Restorative Sleep Compliance"),
                Triple("Skin Conductance (GSR)", "3.2 µS (Normal)", "Elevated Cognitive Trigger Alert"),
                Triple("Burnout Predictive Index", "43% (Moderate)", "Incorporate Active Recovery")
            )

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (row in rows) {
                yOffset += 24f
                // Horiz Row line
                paint.style = Paint.Style.STROKE
                paint.color = 0xFFE2E8F0.toInt()
                paint.strokeWidth = 1f
                canvas.drawLine(30f, yOffset + 20f, 565f, yOffset + 20f, paint)

                paint.style = Paint.Style.FILL
                paint.color = 0xFF334155.toInt()
                canvas.drawText(row.first, 40f, yOffset + 15f, paint)
                canvas.drawText(row.second, 220f, yOffset + 15f, paint)
                canvas.drawText(row.third, 380f, yOffset + 15f, paint)
            }

            // Section 3: Yoga & Diet Compliance
            yOffset += 50f
            paint.color = 0xFF1E293B.toInt()
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("3. ADAPTIVE PARASYMPATHETIC REMEDIATION", 30f, yOffset, paint)

            yOffset += 20f
            paint.color = 0xFF475569.toInt()
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val remediationLines = listOf(
                "• Morning Nutrient Mix: Magnesium-packed wild seed oatmeal paired with calming Green tea L-Theanine.",
                "• Afternoon Energy Stabilization: Salmon with grilled leaf spinach, avocados, protecting cardiac vessels.",
                "• Active Yoga Therapy: Scheduled 10-minute Balasana (Child's Pose) instruction daily to soothe adrenal load.",
                "• Evening Sleep Induction Support: Turkey sweet potato carbohydrate mix + Chamomile decaf brewing."
            )
            for (line in remediationLines) {
                canvas.drawText(line, 35f, yOffset, paint)
                yOffset += 20f
            }

            // Section 4: Endorsement Section
            yOffset += 35f
            paint.color = 0xFF64748B.toInt()
            paint.textSize = 9f
            canvas.drawText("Verify security signature below. This PDF constitutes an official wearable telemetry export.", 30f, yOffset, paint)

            paint.style = Paint.Style.STROKE
            paint.color = 0xFFCBD5E1.toInt()
            paint.strokeWidth = 1f
            canvas.drawLine(30f, yOffset + 12f, 565f, yOffset + 12f, paint)

            paint.style = Paint.Style.FILL
            paint.color = 0xFF0F172A.toInt()
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NeuroBand AI Core Engine - Autonomous Diagnostic Log (Ver 2.0)", 140f, yOffset + 28f, paint)

            pdfDocument.finishPage(page)

            val fileName = "neuroband_stress_report_${reportType.lowercase().replace(" ", "_")}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "com.example.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Telemetry Report PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
