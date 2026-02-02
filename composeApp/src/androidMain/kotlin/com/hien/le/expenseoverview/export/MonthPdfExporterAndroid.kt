package com.hien.le.expenseoverview.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

class MonthPdfExporterAndroid(
    private val context: Context
) : MonthPdfExporter {

    override suspend fun exportMonthPdf(
        monthLabel: String,
        fromDateIso: String,
        toDateIso: String,
        rows: List<SummaryRowUi>,
        receipts: List<ReceiptLine>
    ): String {
        val doc = PdfDocument()

        // A4-ish
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36
        var y = margin

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val smallPaint = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        fun newPage(pageNo: Int): PdfDocument.Page {
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create()
            val page = doc.startPage(info)
            y = margin
            return page
        }

        fun ensureSpace(currentPage: PdfDocument.Page, lines: Int): PdfDocument.Page {
            val needed = lines * 14
            return if (y + needed > pageHeight - margin) {
                doc.finishPage(currentPage)
                newPage( 1)
            } else currentPage
        }

        var page = newPage(1)

        // Title
        page.canvas.drawText("Kassenabrechnung $monthLabel", margin.toFloat(), y.toFloat(), titlePaint)
        y += 22
        page.canvas.drawText("Zeitraum: $fromDateIso → $toDateIso", margin.toFloat(), y.toFloat(), smallPaint)
        y += 18

        // Table header
        page = ensureSpace(page, 2)
        page.canvas.drawText(
            "DATE       | BARGELD     | KARTE       | EXPENSE     | NET",
            margin.toFloat(), y.toFloat(), headerPaint
        )
        y += 14
        page.canvas.drawText(
            "-----------+-------------+-------------+-------------+------------",
            margin.toFloat(), y.toFloat(), headerPaint
        )
        y += 16

        val receiptsByDate = receipts.groupBy { it.dateIso }

        rows.forEach { r ->
            page = ensureSpace(page, 2)

            val line = buildString {
                append(r.dateIso.padEnd(10))
                append(" | ")
                append(MoneyFormatter.centsToDeEuro(r.bargeldCents).padEnd(11))
                append(" | ")
                append(MoneyFormatter.centsToDeEuro(r.karteCents).padEnd(11))
                append(" | ")
                append(MoneyFormatter.centsToDeEuro(r.expenseCents).padEnd(11))
                append(" | ")
                append(MoneyFormatter.centsToDeEuro(r.netCents))
            }
            page.canvas.drawText(line, margin.toFloat(), y.toFloat(), textPaint)
            y += 16

            val list = receiptsByDate[r.dateIso].orEmpty()
            if (list.isNotEmpty()) {
                list.forEach { rec ->
                    page = ensureSpace(page, 1)
                    val recLine = "  • ${rec.vendorName} — ${MoneyFormatter.centsToDeEuro(rec.amountCents)}"
                    page.canvas.drawText(recLine, margin.toFloat(), y.toFloat(), smallPaint)
                    y += 14
                }
                y += 6
            }
        }

        doc.finishPage(page)

        // ✅ Save via MediaStore -> Documents/ExpenseOverview/
        val fileName = "summary_${fromDateIso}_to_${toDateIso}.pdf"
        val mime = "application/pdf"

        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/ExpenseOverview")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(collection, values)
            ?: run {
                doc.close()
                error("Cannot create MediaStore record")
            }

        resolver.openOutputStream(uri, "w")!!.use { out ->
            doc.writeTo(out)
        }
        doc.close()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        // Trả về uri string để bạn show cho user / share
        return uri.toString()
    }
}