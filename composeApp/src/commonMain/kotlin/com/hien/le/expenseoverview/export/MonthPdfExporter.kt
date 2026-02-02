package com.hien.le.expenseoverview.export

import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

data class ReceiptLine(
    val dateIso: String,
    val vendorName: String,
    val amountCents: Long
)

interface MonthPdfExporter {
    /**
     * @return absolute path of generated PDF
     */
    suspend fun exportMonthPdf(
        monthLabel: String,                  // e.g. "Tháng 2"
        fromDateIso: String,                 // yyyy-mm-dd
        toDateIso: String,                   // yyyy-mm-dd
        rows: List<SummaryRowUi>,            // daily summary rows
        receipts: List<ReceiptLine>          // all receipts in range
    ): String
}