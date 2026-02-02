package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.summary.*
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.SummaryTable
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun SummaryScreen(vm: SummaryViewModel) {
    val state by vm.state.collectAsState()
    val todayIso = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tổng kết", style = MaterialTheme.typography.headlineSmall)

        DateQuickPicker(
            selectedDateIso = state.anchorDateIso.ifBlank { todayIso },
            onSelectDateIso = { iso -> vm.dispatch(SummaryAction.SelectDay(iso)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { vm.dispatch(SummaryAction.SelectCurrentMonth) }) {
                Text("Tháng")
            }

            Spacer(Modifier.width(8.dp))

            // ✅ ONE export button
            Button(
                onClick = { vm.dispatch(SummaryAction.ExportMonthPdf) },
                enabled = (state.mode == SummaryMode.MONTH) && !state.isExporting
            ) {
                Text(if (state.isExporting) "Đang export..." else "Export PDF")
            }

            Spacer(Modifier.weight(1f))

            MonthDropdown(
                selectedMonthNumber = state.selectedMonthNumber,
                onMonthSelected = { m -> vm.dispatch(SummaryAction.SelectMonth(m)) }
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (state.rows.isEmpty() && !state.isLoading) {
            Text("Chưa có dữ liệu.")
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val sticky = maxWidth >= 600.dp && state.mode == SummaryMode.MONTH
                SummaryTable(
                    rows = state.rows,
                    stickyHeaderEnabled = sticky,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // export result dialog
        if (state.exportResultMessage != null) {
            AlertDialog(
                onDismissRequest = { vm.dispatch(SummaryAction.ClearExportMessage) },
                title = { Text("Export") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.exportResultMessage!!)
                        if (!state.exportPath.isNullOrBlank()) {
                            Text("File: ${state.exportPath}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { vm.dispatch(SummaryAction.ClearExportMessage) }) {
                        Text("OK")
                    }
                }
            )
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdown(
    selectedMonthNumber: Int,
    onMonthSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "Tháng $selectedMonthNumber",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Chọn tháng") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().widthIn(min = 160.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..12).forEach { m ->
                DropdownMenuItem(
                    text = { Text("Tháng $m") },
                    onClick = {
                        onMonthSelected(m)
                        expanded = false
                    }
                )
            }
        }
    }
}