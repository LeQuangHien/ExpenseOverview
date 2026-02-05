package com.hien.le.expenseoverview.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hien.le.expenseoverview.AppContainer
import com.hien.le.expenseoverview.presentation.audit.AuditLogViewModel
import com.hien.le.expenseoverview.presentation.entry.EntryViewModel
import com.hien.le.expenseoverview.presentation.summary.SummaryViewModel
import com.hien.le.expenseoverview.presentation.entry.EntryScreen
import com.hien.le.expenseoverview.ui.screens.AuditLogScreen
import com.hien.le.expenseoverview.presentation.summary.SummaryScreen

@Composable
fun AppEntryPoint(container: AppContainer) {
    val entryVm = viewModel<EntryViewModel>(factory = entryViewModelFactory(container))
    val summaryVm = viewModel<SummaryViewModel>(factory = summaryViewModelFactory(container))
    val auditVm = viewModel<AuditLogViewModel>(factory = auditViewModelFactory(container))

    AppRoot(
        entryContent = { EntryScreen(entryVm) },
        summaryContent = { SummaryScreen(summaryVm) },
        auditContent = { AuditLogScreen(auditVm) }
    )
}