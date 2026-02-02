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
            getEntryWithExpenses = container.getEntryWithExpenses,
            saveDailyEntryWithExpenses = container.saveDailyEntryWithExpenses,
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
            repo = container.auditRepo,
            dispatchers = container.dispatchers
        )
    }
}