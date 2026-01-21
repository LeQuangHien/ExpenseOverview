package com.hien.le.expenseoverview

import android.content.Context
import com.hien.le.expenseoverview.data.repository.AuditRepositoryImpl
import com.hien.le.expenseoverview.data.repository.EntryRepositoryImpl
import com.hien.le.expenseoverview.db.buildDatabase
import com.hien.le.expenseoverview.db.getDatabaseBuilder
import com.hien.le.expenseoverview.domain.usecase.*
import com.hien.le.expenseoverview.platform.ClockAndroid
import com.hien.le.expenseoverview.presentation.audit.AuditLogViewModel
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import com.hien.le.expenseoverview.presentation.entry.EntryViewModel
import com.hien.le.expenseoverview.presentation.summary.SummaryViewModel

class AppGraphAndroid(context: Context) {
    private val clock = ClockAndroid()
    private val dispatchers = CoroutineDispatchers()

    private val db = buildDatabase(getDatabaseBuilder(context))
    private val dao = db.expenseDao()

    private val entryRepo = EntryRepositoryImpl(dao)
    private val auditRepo = AuditRepositoryImpl(dao)

    private val getDailyEntry = GetDailyEntry(entryRepo)
    private val upsertWithAudit = UpsertDailyEntryWithAudit(entryRepo, auditRepo, clock)
    private val getSummary = GetSummary(entryRepo)
    private val getAuditEvents = GetAuditEvents(auditRepo)
    private val purgeOldAudit = PurgeOldAudit(auditRepo, clock)

    fun entryVm() = EntryViewModel(getDailyEntry, upsertWithAudit, clock, dispatchers)
    fun summaryVm() = SummaryViewModel(getSummary, dispatchers)
    fun auditVm() = AuditLogViewModel(getAuditEvents, purgeOldAudit, dispatchers)
}