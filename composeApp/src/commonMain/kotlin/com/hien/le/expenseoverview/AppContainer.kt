package com.hien.le.expenseoverview

import GetEntryWithExpensesUseCase
import com.hien.le.expenseoverview.domain.repository.AuditRepository
import com.hien.le.expenseoverview.domain.repository.SummaryRepository
import com.hien.le.expenseoverview.domain.usecase.*
import com.hien.le.expenseoverview.export.MonthPdfExporter
import com.hien.le.expenseoverview.platform.Clock
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers

interface AppContainer {
    val getDailyEntry: GetDailyEntry
    val upsertDailyEntryWithAudit: UpsertDailyEntryWithAudit
    val getSummary: GetSummary
    val getAuditEvents: GetAuditEvents
    val purgeOldAudit: PurgeOldAudit
    val getExpenseItemsByDate: GetExpenseItemsByDate
    val addExpenseItem: AddExpenseItem
    val deleteExpenseItem: DeleteExpenseItem
    val saveDailyEntryWithExpenses: SaveDailyEntryWithExpensesUseCase
    val getEntryWithExpenses: GetEntryWithExpensesUseCase
    val monthPdfExporter: MonthPdfExporter

    val auditRepo: AuditRepository
    val summaryRepo: SummaryRepository

    val clock: Clock
    val dispatchers: CoroutineDispatchers
}