package com.hien.le.expenseoverview.data.repository

import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.model.Summary
import com.hien.le.expenseoverview.domain.repository.ExportRepository

class ExportRepositoryImpl : ExportRepository {
    override suspend fun exportDailyCsv(entry: DailyEntry, path: String): String = path
    override suspend fun exportSummaryCsv(summary: Summary, path: String): String = path
    override suspend fun exportAuditCsv(events: List<AuditEvent>, path: String): String = path
    override suspend fun exportDailyPdf(entry: DailyEntry, path: String): String = path
    override suspend fun exportSummaryPdf(summary: Summary, path: String): String = path
    override suspend fun exportAuditPdf(events: List<AuditEvent>, path: String): String = path
}