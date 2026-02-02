package com.hien.le.expenseoverview.domain.repository

import com.hien.le.expenseoverview.export.ReceiptLine
import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

interface SummaryRepository {
    suspend fun getSummaryRows(fromDateIso: String, toDateIso: String): List<SummaryRowUi>
    suspend fun getReceiptsInRange(fromDateIso: String, toDateIso: String): List<ReceiptLine>
}