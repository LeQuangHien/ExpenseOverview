package com.hien.le.expenseoverview.platform

import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.model.Summary

interface PdfGenerator {
    fun dailyReport(entry: DailyEntry): ByteArray
    fun monthlyReport(summary: Summary): ByteArray
    fun auditReport(events: List<AuditEvent>): ByteArray
}