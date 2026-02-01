package com.hien.le.expenseoverview.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hien.le.expenseoverview.AppContainer
import com.hien.le.expenseoverview.presentation.audit.AuditLogViewModel
import com.hien.le.expenseoverview.presentation.entry.EntryViewModel
import com.hien.le.expenseoverview.presentation.summary.SummaryViewModel

fun entryViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer {
        EntryViewModel(
            getDailyEntry = container.getDailyEntry,
            upsertWithAudit = container.upsertDailyEntryWithAudit,
            getExpenseItemsByDate = container.getExpenseItemsByDate,
            addExpenseItem = container.addExpenseItem,
            deleteExpenseItem = container.deleteExpenseItem,
            clock = container.clock,
            dispatchers = container.dispatchers
        )
    }
}

fun summaryViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer {
        SummaryViewModel(
            getSummary = container.getSummary,
            dispatchers = container.dispatchers
        )
    }
}

fun auditViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer {
        AuditLogViewModel(
            getAuditEvents = container.getAuditEvents,
            purgeOldAudit = container.purgeOldAudit,
            dispatchers = container.dispatchers
        )
    }
}