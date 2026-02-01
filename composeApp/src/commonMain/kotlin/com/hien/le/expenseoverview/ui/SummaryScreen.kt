package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.domain.model.SummaryRange
import com.hien.le.expenseoverview.presentation.summary.SummaryAction
import com.hien.le.expenseoverview.presentation.summary.SummaryViewModel
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.SummaryTable

@Composable
fun SummaryScreen(vm: SummaryViewModel) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tổng kết", style = MaterialTheme.typography.headlineSmall)

        DateQuickPicker(
            selectedDateIso = state.anchorDateIso,
            onSelectDateIso = { iso ->
                vm.dispatch(SummaryAction.Load(state.range, iso))
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.range == SummaryRange.DAY,
                onClick = { vm.dispatch(SummaryAction.Load(SummaryRange.DAY, state.anchorDateIso)) },
                label = { Text("Ngày") }
            )
            FilterChip(
                selected = state.range == SummaryRange.WEEK,
                onClick = { vm.dispatch(SummaryAction.Load(SummaryRange.WEEK, state.anchorDateIso)) },
                label = { Text("Tuần") }
            )
            FilterChip(
                selected = state.range == SummaryRange.MONTH,
                onClick = { vm.dispatch(SummaryAction.Load(SummaryRange.MONTH, state.anchorDateIso)) },
                label = { Text("Tháng") }
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        val summary = state.summary
        if (summary == null) {
            Text("Chưa có dữ liệu (hoặc chưa load).")
            if (state.errorMessage != null) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            return
        }

        // Tổng quan
        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Range: ${summary.fromDateIso} → ${summary.toDateIso}")
                Text("Rows: ${summary.rows.size}")
            }
        }

        // Table: sticky header trên tablet
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sticky = maxWidth >= 600.dp
            SummaryTable(
                rows = summary.rows,
                stickyHeaderEnabled = sticky,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}