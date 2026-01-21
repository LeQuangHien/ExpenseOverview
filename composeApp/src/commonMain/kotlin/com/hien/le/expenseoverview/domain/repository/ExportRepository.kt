package com.hien.le.expenseoverview.domain.repository

import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.model.Summary

interface ExportRepository {
    suspend fun exportDailyCsv(entry: DailyEntry, path: String): String
    suspend fun exportSummaryCsv(summary: Summary, path: String): String
    suspend fun exportAuditCsv(events: List<AuditEvent>, path: String): String

    suspend fun exportDailyPdf(entry: DailyEntry, path: String): String
    suspend fun exportSummaryPdf(summary: Summary, path: String): String
    suspend fun exportAuditPdf(events: List<AuditEvent>, path: String): String
}