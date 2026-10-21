package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.FeeRecordEntity
import com.example.data.MonthlyReportEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceiptGenerator {

    fun generateFeeReceiptPdf(context: Context, fee: FeeRecordEntity): File {
        val receiptsDir = File(context.cacheDir, "receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }
        val safeName = fee.studentName.replace(Regex("[^A-Za-z0-9]"), "_").ifEmpty { "Student" }
        val pdfFile = File(receiptsDir, "AlHadid_FeeReceipt_${safeName}_${fee.id}.pdf")

        val drawnWithNativePdf = runCatching {
            val pdfDocument = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                val greenColor = Color.parseColor("#115E27")
                val darkGreenColor = Color.parseColor("#0D471D")
                val goldColor = Color.parseColor("#C49A23")
                val creamColor = Color.parseColor("#FAF7EE")
                val textDark = Color.parseColor("#1E2923")
                val textMuted = Color.parseColor("#64746B")

                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = creamColor }
                canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = goldColor
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRoundRect(RectF(24f, 24f, 571f, 818f), 16f, 16f, borderPaint)

                val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = greenColor }
                canvas.drawRoundRect(RectF(36f, 36f, 559f, 175f), 14f, 14f, headerPaint)

                drawLogoOnCanvas(canvas, 88f, 105f, 34f, greenColor, goldColor)

                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 25f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("AL HADID ACADEMY", 315f, 82f, titlePaint)

                val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FACC15")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Nasirabad Jatlan, Azad Kashmir", 315f, 108f, subTitlePaint)

                val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E2F5E6")
                    textSize = 11.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "Complete Islamic & Modern Education System • Official Fee Receipt",
                    315f,
                    132f,
                    taglinePaint
                )
                canvas.drawText(
                    "Under Supervision of Principal Awais Mustafa & Quran Dept",
                    315f,
                    152f,
                    taglinePaint
                )

                val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                canvas.drawRoundRect(RectF(36f, 195f, 559f, 265f), 12f, 12f, cardPaint)
                canvas.drawRoundRect(
                    RectF(36f, 195f, 559f, 265f),
                    12f,
                    12f,
                    borderPaint.apply { strokeWidth = 1.2f }
                )

                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textMuted
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textDark
                    textSize = 14f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }

                val receiptNo = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                val dateText = dateFormat.format(Date(fee.paidAt ?: System.currentTimeMillis()))

                canvas.drawText("RECEIPT NO", 56f, 220f, labelPaint)
                canvas.drawText(receiptNo, 56f, 244f, valuePaint)

                canvas.drawText("DATE ISSUED", 220f, 220f, labelPaint)
                canvas.drawText(dateText, 220f, 244f, valuePaint)

                val statusBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (fee.isPaid) greenColor else Color.parseColor("#B45309")
                }
                canvas.drawRoundRect(RectF(435f, 210f, 542f, 248f), 10f, 10f, statusBgPaint)
                val statusTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 13f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(if (fee.isPaid) "PAID" else "PENDING", 488.5f, 234f, statusTextPaint)

                canvas.drawRoundRect(RectF(36f, 285f, 559f, 475f), 12f, 12f, cardPaint)
                canvas.drawRoundRect(RectF(36f, 285f, 559f, 475f), 12f, 12f, borderPaint)

                val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = greenColor
                    textSize = 15f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                canvas.drawText("Student & Enrolment Particulars", 56f, 316f, sectionPaint)

                val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E6DFC8")
                    strokeWidth = 1f
                }
                canvas.drawLine(56f, 328f, 539f, 328f, dividerPaint)

                fun drawRow(
                    y: Float,
                    leftLabel: String,
                    leftValue: String,
                    rightLabel: String,
                    rightValue: String
                ) {
                    canvas.drawText(leftLabel, 56f, y, labelPaint)
                    canvas.drawText(leftValue, 56f, y + 20f, valuePaint)
                    canvas.drawText(rightLabel, 320f, y, labelPaint)
                    canvas.drawText(rightValue, 320f, y + 20f, valuePaint)
                }

                drawRow(
                    352f,
                    "STUDENT NAME",
                    fee.studentName,
                    "FATHER / GUARDIAN",
                    fee.fatherName.ifEmpty { "Registered Guardian" }
                )
                drawRow(
                    412f,
                    "CLASS / GROUP",
                    fee.className.ifEmpty { "Regular" },
                    "ACADEMY DEPARTMENT",
                    fee.department
                )

                canvas.drawRoundRect(RectF(36f, 495f, 559f, 645f), 12f, 12f, cardPaint)
                canvas.drawRoundRect(RectF(36f, 495f, 559f, 645f), 12f, 12f, borderPaint)

                canvas.drawText("Fee Statement (${fee.monthCycle})", 56f, 526f, sectionPaint)
                canvas.drawLine(56f, 538f, 539f, 538f, dividerPaint)

                canvas.drawText("Description", 56f, 562f, labelPaint)
                canvas.drawText("Amount (PKR)", 445f, 562f, labelPaint)

                canvas.drawText("Monthly Academy & Tuition Fee - ${fee.department}", 56f, 590f, valuePaint)
                canvas.drawText("Rs ${fee.amount}", 445f, 590f, valuePaint)

                val totalStripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EAF5EC") }
                canvas.drawRoundRect(RectF(52f, 604f, 543f, 634f), 8f, 8f, totalStripPaint)
                val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = darkGreenColor
                    textSize = 15f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(
                    if (fee.isPaid) "TOTAL FEE RECEIVED" else "TOTAL FEE PAYABLE",
                    66f,
                    624f,
                    totalPaint
                )
                canvas.drawText("Rs ${fee.amount} /-", 445f, 624f, totalPaint)

                canvas.drawLine(64f, 725f, 230f, 725f, dividerPaint)
                canvas.drawLine(365f, 725f, 531f, 725f, dividerPaint)
                canvas.drawText("Principal Awais Mustafa", 82f, 743f, labelPaint)
                canvas.drawText("Academy Administration", 390f, 743f, labelPaint)

                val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#9E7B1C")
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "Al Hadid Academy • Nasirabad Jatlan, Azad Kashmir",
                    297.5f,
                    780f,
                    footerPaint
                )
                canvas.drawText(
                    "Software by anas mustafa  |  03700880220  •  03129484214",
                    297.5f,
                    798f,
                    footerPaint
                )

                pdfDocument.finishPage(page)
                FileOutputStream(pdfFile).use { out ->
                    pdfDocument.writeTo(out)
                }
            } finally {
                runCatching { pdfDocument.close() }
            }
            true
        }.getOrDefault(false)

        if (!drawnWithNativePdf || !pdfFile.exists() || pdfFile.length() == 0L) {
            writeStandardPdfFallback(pdfFile, fee)
        }
        return pdfFile
    }

    fun generateMonthlyReportPdf(context: Context, report: MonthlyReportEntity): File {
        val reportsDir = File(context.cacheDir, "receipts")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val safeName = report.studentName.replace(Regex("[^A-Za-z0-9]"), "_").ifEmpty { "Student" }
        val pdfFile = File(reportsDir, "AlHadid_MonthlyReport_${safeName}_${report.id}.pdf")

        val drawnWithNativePdf = runCatching {
            val pdfDocument = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val greenColor = Color.parseColor("#115E27")
                val goldColor = Color.parseColor("#C49A23")
                val creamColor = Color.parseColor("#FAF7EE")
                val textDark = Color.parseColor("#1E2923")
                val textMuted = Color.parseColor("#64746B")

                canvas.drawRect(0f, 0f, 595f, 842f, Paint().apply { color = creamColor })

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = goldColor
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
                canvas.drawRoundRect(RectF(24f, 24f, 571f, 818f), 16f, 16f, borderPaint)

                // Header with Logo
                val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = greenColor }
                canvas.drawRoundRect(RectF(36f, 36f, 559f, 155f), 14f, 14f, headerPaint)

                drawLogoOnCanvas(canvas, 86f, 95f, 34f, greenColor, goldColor)

                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 23f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("AL HADID ACADEMY", 320f, 75f, titlePaint)

                val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FACC15")
                    textSize = 13f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Nasirabad Jatlan, Azad Kashmir • Monthly Student Report", 320f, 100f, subTitlePaint)

                val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 11.5f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "Student: ${report.studentName}  |  Class: ${report.className}  |  Date: ${report.dateString}",
                    320f,
                    126f,
                    metaPaint
                )

                val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                canvas.drawRoundRect(RectF(36f, 170f, 559f, 650f), 12f, 12f, cardPaint)
                canvas.drawRoundRect(RectF(36f, 170f, 559f, 650f), 12f, 12f, borderPaint.apply { strokeWidth = 1.2f })

                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = greenColor
                    textSize = 12f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textDark
                    textSize = 12f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E6DFC8")
                    strokeWidth = 1f
                }

                var y = 198f
                fun drawReportItem(num: String, title: String, value: String) {
                    canvas.drawText("$num. $title:", 52f, y, labelPaint)
                    canvas.drawText(value.ifBlank { "Satisfactory" }.take(80), 52f, y + 18f, valPaint)
                    canvas.drawLine(52f, y + 30f, 540f, y + 30f, dividerPaint)
                    y += 54f
                }

                drawReportItem("1", "Remark of Quran Teacher", report.quranTeacherRemark)
                drawReportItem("2", "Remarks of Principal", report.principalRemark)
                drawReportItem("3", "Remarks of Class Teacher", report.classTeacherRemark)
                drawReportItem("4", "Behavior with class fellows", report.behaviorWithFellows)
                drawReportItem("5", "Behavior with teachers", report.behaviorWithTeachers)
                drawReportItem("6", "Need attention in", report.needAttentionIn.ifBlank { "None" })
                drawReportItem(
                    "7",
                    "Did participate in Mehfil Milad Pak?",
                    if (report.participatedInMilad) "Yes" else "No"
                )

                // 8. Signatures Row: 3 signatures with label below
                val sigBoxY = 665f
                canvas.drawRoundRect(RectF(36f, sigBoxY, 559f, 765f), 12f, 12f, cardPaint)
                canvas.drawRoundRect(RectF(36f, sigBoxY, 559f, 765f), 12f, 12f, borderPaint)

                val sigScriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#115E27")
                    textSize = 18f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
                    textAlign = Paint.Align.CENTER
                }
                val sigLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textDark
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val sigSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textMuted
                    textSize = 9.5f
                    textAlign = Paint.Align.CENTER
                }

                // Left: Class Teacher Sign (stylish female signature) + "Female Teacher"
                canvas.drawText("F. Teacher", 122f, sigBoxY + 42f, sigScriptPaint)
                canvas.drawLine(62f, sigBoxY + 52f, 182f, sigBoxY + 52f, dividerPaint)
                canvas.drawText("Female Teacher", 122f, sigBoxY + 70f, sigLabelPaint)
                canvas.drawText("Class Teacher Sign", 122f, sigBoxY + 84f, sigSubPaint)

                // Center: Quran Teacher Sign + "anas mustafa" (Quran Teacher)
                canvas.drawText("anas mustafa", 297.5f, sigBoxY + 42f, sigScriptPaint)
                canvas.drawLine(237f, sigBoxY + 52f, 357f, sigBoxY + 52f, dividerPaint)
                canvas.drawText("anas mustafa", 297.5f, sigBoxY + 70f, sigLabelPaint)
                canvas.drawText("Quran Teacher Sign", 297.5f, sigBoxY + 84f, sigSubPaint)

                // Right: Principal Sign + "Awais" (Principal)
                canvas.drawText("P. Awais", 472f, sigBoxY + 42f, sigScriptPaint)
                canvas.drawLine(412f, sigBoxY + 52f, 532f, sigBoxY + 52f, dividerPaint)
                canvas.drawText("Awais", 472f, sigBoxY + 70f, sigLabelPaint)
                canvas.drawText("Principal Sign", 472f, sigBoxY + 84f, sigSubPaint)

                val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#9E7B1C")
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "Al Hadid Academy • Nasirabad Jatlan, Azad Kashmir • anas mustafa (03700880220 | 03129484214)",
                    297.5f,
                    795f,
                    footerPaint
                )

                pdfDocument.finishPage(page)
                FileOutputStream(pdfFile).use { out ->
                    pdfDocument.writeTo(out)
                }
            } finally {
                runCatching { pdfDocument.close() }
            }
            true
        }.getOrDefault(false)

        if (!drawnWithNativePdf || !pdfFile.exists() || pdfFile.length() == 0L) {
            writeStandardReportPdfFallback(pdfFile, report)
        }
        return pdfFile
    }

    private fun drawLogoOnCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        greenColor: Int,
        goldColor: Int
    ) {
        val whiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val goldStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val greenFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = greenColor }
        canvas.drawCircle(cx, cy, radius, whiteFill)
        canvas.drawCircle(cx, cy, radius, goldStroke)
        canvas.drawRoundRect(
            RectF(cx - radius * 0.45f, cy - radius * 0.35f, cx + radius * 0.45f, cy + radius * 0.1f),
            radius * 0.4f,
            radius * 0.4f,
            greenFill
        )
        val logoText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = greenColor
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Al Hadid", cx, cy + radius * 0.48f, logoText)
    }

    private fun writeStandardPdfFallback(pdfFile: File, fee: FeeRecordEntity) {
        val receiptNo = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }
        val status = if (fee.isPaid) "PAID" else "PENDING"
        val streamContent = buildString {
            append("BT /F1 18 Tf 50 770 Td (AL HADID ACADEMY - FEE RECEIPT) Tj ")
            append("/F1 12 Tf 0 -24 Td (Nasirabad Jatlan, Azad Kashmir) Tj ")
            append("0 -28 Td (Receipt No: $receiptNo | Status: $status) Tj ")
            append("0 -22 Td (Student Name: ${fee.studentName}) Tj ")
            append("0 -22 Td (Class & Dept: ${fee.className} - ${fee.department}) Tj ")
            append("0 -22 Td (Fee Cycle: ${fee.monthCycle}) Tj ")
            append("0 -22 Td (Amount: Rs ${fee.amount}) Tj ")
            append("0 -30 Td (anas mustafa | 03700880220 | 03129484214) Tj ET")
        }
        writeRawPdf(pdfFile, streamContent)
    }

    private fun writeStandardReportPdfFallback(pdfFile: File, report: MonthlyReportEntity) {
        val streamContent = buildString {
            append("BT /F1 16 Tf 50 780 Td (AL HADID ACADEMY - MONTHLY REPORT) Tj ")
            append("/F1 11 Tf 0 -22 Td (Student: ${report.studentName} | Class: ${report.className}) Tj ")
            append("0 -20 Td (1. Quran Teacher Remark: ${report.quranTeacherRemark}) Tj ")
            append("0 -20 Td (2. Principal Remark: ${report.principalRemark}) Tj ")
            append("0 -20 Td (3. Class Teacher Remark: ${report.classTeacherRemark}) Tj ")
            append("0 -20 Td (4. Behavior with Fellows: ${report.behaviorWithFellows}) Tj ")
            append("0 -20 Td (5. Behavior with Teachers: ${report.behaviorWithTeachers}) Tj ")
            append("0 -20 Td (6. Need Attention In: ${report.needAttentionIn}) Tj ")
            append("0 -20 Td (7. Participated in Mehfil Milad Pak: ${if (report.participatedInMilad) "Yes" else "No"}) Tj ")
            append("0 -24 Td (Signatures: Female Teacher | anas mustafa | Awais) Tj ET")
        }
        writeRawPdf(pdfFile, streamContent)
    }

    private fun writeRawPdf(pdfFile: File, streamContent: String) {
        val rawPdf = """
            %PDF-1.4
            1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
            2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
            3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
            4 0 obj << /Length ${streamContent.length} >> stream
            $streamContent
            endstream endobj
            5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> endobj
            xref
            0 6
            0000000000 65535 f 
            trailer << /Size 6 /Root 1 0 R >>
            %%EOF
        """.trimIndent()
        pdfFile.writeText(rawPdf)
    }

    fun shareReceiptToWhatsApp(context: Context, fee: FeeRecordEntity, directWhatsApp: Boolean = true): File {
        val pdfFile = generateFeeReceiptPdf(context, fee)
        val receiptNo = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }
        val statusText = if (fee.isPaid) "PAID (Alhamdulillah)" else "PENDING DUES"
        val shareMessage = buildString {
            append("*AL HADID ACADEMY*\n")
            append("_Nasirabad Jatlan, Azad Kashmir_\n\n")
            append("*Official Fee Receipt: $receiptNo*\n")
            append("• Student: *${fee.studentName}*\n")
            append("• Class/Dept: ${fee.className} (${fee.department})\n")
            append("• Cycle: ${fee.monthCycle}\n")
            append("• Amount: *Rs ${fee.amount}*\n")
            append("• Status: *$statusText*\n\n")
            append("_anas mustafa: 03700880220 | 03129484214_")
        }
        sharePdfFile(context, pdfFile, "Al Hadid Academy Fee Receipt - ${fee.studentName}", shareMessage, directWhatsApp)
        return pdfFile
    }

    fun shareMonthlyReportPdf(context: Context, report: MonthlyReportEntity, directWhatsApp: Boolean = false): File {
        val pdfFile = generateMonthlyReportPdf(context, report)
        val shareMessage = buildString {
            append("*AL HADID ACADEMY - MONTHLY REPORT*\n")
            append("• Student: *${report.studentName}* (${report.className})\n")
            append("• Quran Teacher Remark: ${report.quranTeacherRemark}\n")
            append("• Principal Remark: ${report.principalRemark}\n")
            append("• Class Teacher Remark: ${report.classTeacherRemark}\n")
            append("• Behavior (Fellows / Teachers): ${report.behaviorWithFellows} / ${report.behaviorWithTeachers}\n")
            append("• Need Attention In: ${report.needAttentionIn.ifBlank { "None" }}\n")
            append("• Mehfil Milad Pak Participation: ${if (report.participatedInMilad) "Yes" else "No"}\n")
            append("• Signed: Female Teacher | anas mustafa (Quran Teacher) | Awais (Principal)")
        }
        sharePdfFile(context, pdfFile, "Monthly Report - ${report.studentName}", shareMessage, directWhatsApp)
        return pdfFile
    }

    private fun sharePdfFile(
        context: Context,
        pdfFile: File,
        subject: String,
        shareMessage: String,
        directWhatsApp: Boolean
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            clipData = ClipData.newRawUri(subject, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pm = context.packageManager
        val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        val installedWhatsApp = if (directWhatsApp) {
            whatsAppPackages.firstOrNull { pkg ->
                runCatching {
                    pm.getPackageInfo(pkg, 0)
                    true
                }.getOrDefault(false)
            }
        } else null

        if (installedWhatsApp != null) {
            sendIntent.setPackage(installedWhatsApp)
            runCatching {
                context.startActivity(sendIntent)
            }.onFailure {
                val chooser = Intent.createChooser(sendIntent.setPackage(null), subject).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } else {
            Toast.makeText(
                context,
                "PDF generated: ${pdfFile.name}",
                Toast.LENGTH_SHORT
            ).show()
            val chooser = Intent.createChooser(sendIntent, subject).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching {
                context.startActivity(chooser)
            }
        }
    }
}
