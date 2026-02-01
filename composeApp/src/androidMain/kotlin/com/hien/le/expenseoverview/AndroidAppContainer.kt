package com.hien.le.expenseoverview

import android.content.Context
import com.hien.le.expenseoverview.data.repository.AuditRepositoryImpl
import com.hien.le.expenseoverview.data.repository.EntryRepositoryImpl
import com.hien.le.expenseoverview.data.repository.ExpenseItemRepositoryImpl
import com.hien.le.expenseoverview.db.buildDatabase
import com.hien.le.expenseoverview.db.getDatabaseBuilder
import com.hien.le.expenseoverview.domain.usecase.*
import com.hien.le.expenseoverview.platform.ClockAndroid
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers

class AndroidAppContainer(context: Context) : AppContainer {
    override val clock = ClockAndroid()
    override val dispatchers = CoroutineDispatchers()

    private val db = buildDatabase(getDatabaseBuilder(context))
    private val dao = db.expenseDao()

    private val entryRepo = EntryRepositoryImpl(dao)
    private val auditRepo = AuditRepositoryImpl(dao)
    private val expenseRepo = ExpenseItemRepositoryImpl(dao)

    override val getDailyEntry = GetDailyEntry(entryRepo)
    override val upsertDailyEntryWithAudit = UpsertDailyEntryWithAudit(entryRepo, auditRepo, clock)
    override val getSummary = GetSummary(entryRepo, expenseRepo)
    override val getAuditEvents = GetAuditEvents(auditRepo)
    override val purgeOldAudit = PurgeOldAudit(auditRepo, clock)
    override val getExpenseItemsByDate = GetExpenseItemsByDate(expenseRepo)
    override val addExpenseItem = AddExpenseItem(expenseRepo, clock)
    override val deleteExpenseItem = DeleteExpenseItem(expenseRepo)
}