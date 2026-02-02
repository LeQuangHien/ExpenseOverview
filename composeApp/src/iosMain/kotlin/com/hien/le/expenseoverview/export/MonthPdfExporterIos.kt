package com.hien.le.expenseoverview.export

import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

class MonthPdfExporterIos : MonthPdfExporter {
    override suspend fun exportMonthPdf(
        monthLabel: String,
        fromDateIso: String,
        toDateIso: String,
        rows: List<SummaryRowUi>,
        receipts: List<ReceiptLine>
    ): String {
        error("PDF export not implemented on iOS yet")
    }
}