package com.hien.le.expenseoverview

import GetEntryWithExpensesUseCase
import android.content.Context
import com.hien.le.expenseoverview.data.repository.AuditRepositoryImpl
import com.hien.le.expenseoverview.data.repository.EntryRepositoryImpl
import com.hien.le.expenseoverview.data.repository.ExpenseItemRepositoryImpl
import com.hien.le.expenseoverview.data.repository.SummaryRepositoryImpl
import com.hien.le.expenseoverview.db.buildDatabase
import com.hien.le.expenseoverview.db.getDatabaseBuilder
import com.hien.le.expenseoverview.domain.usecase.AddExpenseItem
import com.hien.le.expenseoverview.domain.usecase.DeleteExpenseItem
import com.hien.le.expenseoverview.domain.usecase.GetAuditEvents
import com.hien.le.expenseoverview.domain.usecase.GetDailyEntry
import com.hien.le.expenseoverview.domain.usecase.GetExpenseItemsByDate
import com.hien.le.expenseoverview.domain.usecase.GetSummary
import com.hien.le.expenseoverview.domain.usecase.PurgeOldAudit
import com.hien.le.expenseoverview.domain.usecase.SaveDailyEntryWithExpensesUseCase
import com.hien.le.expenseoverview.domain.usecase.UpsertDailyEntryWithAudit
import com.hien.le.expenseoverview.export.MonthPdfExporter
import com.hien.le.expenseoverview.export.MonthPdfExporterAndroid
import com.hien.le.expenseoverview.platform.ClockAndroid
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers

class AndroidAppContainer(context: Context) : AppContainer {
    override val clock = ClockAndroid()
    override val dispatchers = CoroutineDispatchers()

    private val db = buildDatabase(getDatabaseBuilder(context))
    private val dao = db.expenseDao()

    private val entryRepo = EntryRepositoryImpl(dao)
    private val expenseRepo = ExpenseItemRepositoryImpl(dao)
    override val monthPdfExporter: MonthPdfExporter = MonthPdfExporterAndroid(context)

    override val auditRepo = AuditRepositoryImpl(dao)
    override val summaryRepo = SummaryRepositoryImpl(dao)

    override val getEntryWithExpenses = GetEntryWithExpensesUseCase(dao)
    override val getDailyEntry = GetDailyEntry(entryRepo)
    override val upsertDailyEntryWithAudit = UpsertDailyEntryWithAudit(entryRepo, auditRepo, clock)
    override val getSummary = GetSummary(entryRepo, expenseRepo)
    override val getAuditEvents = GetAuditEvents(auditRepo)
    override val purgeOldAudit = PurgeOldAudit(auditRepo, clock)
    override val getExpenseItemsByDate = GetExpenseItemsByDate(expenseRepo)
    override val addExpenseItem = AddExpenseItem(expenseRepo, clock)
    override val deleteExpenseItem = DeleteExpenseItem(expenseRepo)
    override val saveDailyEntryWithExpenses = SaveDailyEntryWithExpensesUseCase(dao, auditRepo)
}