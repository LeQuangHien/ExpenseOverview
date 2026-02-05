package com.hien.le.expenseoverview.presentation.summary

import com.hien.le.expenseoverview.export.ReceiptLine

data class SummaryRowUi(
    val dateIso: String,
    val bargeldCents: Long,
    val karteCents: Long,
    val expenseCents: Long,
    val netCents: Long
)

enum class SummaryMode { DAY, MONTH }

data class SummaryState(
    val mode: SummaryMode = SummaryMode.MONTH,
    val anchorDateIso: String = "",
    val selectedMonthNumber: Int = 1,
    val receipts: List<ReceiptLine> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val rows: List<SummaryRowUi> = emptyList(),

    // export
    val isExporting: Boolean = false,
    val exportResultMessage: String? = null,
    val exportPath: String? = null
)

sealed interface SummaryAction {
    data class SelectDay(val dateIso: String) : SummaryAction

    data object SelectCurrentMonth : SummaryAction
    data class SelectMonth(val monthNumber: Int) : SummaryAction

    data object ExportMonthPdf : SummaryAction
    data object ClearExportMessage : SummaryAction

    data object ClearError : SummaryAction
}