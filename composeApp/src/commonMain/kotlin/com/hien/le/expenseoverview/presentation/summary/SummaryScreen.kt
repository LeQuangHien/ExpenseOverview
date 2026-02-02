package com.hien.le.expenseoverview.presentation.summary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.SummaryTable
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun SummaryScreen(vm: SummaryViewModel) {
    val state by vm.state.collectAsState()

    val todayIso = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }

    // default load: current month
    LaunchedEffect(Unit) {
        if (state.anchorDateIso.isBlank()) {
            vm.dispatch(SummaryAction.SelectCurrentMonth)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tổng kết", style = MaterialTheme.typography.headlineSmall)

        // ✅ DateQuickPicker = DAY mode actions
        DateQuickPicker(
            selectedDateIso = state.anchorDateIso.ifBlank { todayIso },
            onSelectDateIso = { iso ->
                vm.dispatch(SummaryAction.SelectDay(iso))
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Month mode button
            Button(onClick = { vm.dispatch(SummaryAction.SelectCurrentMonth) }) {
                Text("Tháng")
            }

            Spacer(Modifier.weight(1f))

            // ✅ Dropdown month (forces MONTH mode)
            MonthDropdown(
                selectedMonthNumber = state.selectedMonthNumber,
                onMonthSelected = { m -> vm.dispatch(SummaryAction.SelectMonth(m)) }
            )
        }

        // Optional: show mode so user understands
        AssistChip(
            onClick = {},
            label = { Text(if (state.mode == SummaryMode.DAY) "Đang xem: 1 ngày" else "Đang xem: 1 tháng") }
        )

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        val summary = state.summary
        if (summary == null) {
            Text("Chưa có dữ liệu.")
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val sticky = maxWidth >= 600.dp && state.mode == SummaryMode.MONTH
                SummaryTable(
                    rows = summary.rows,
                    stickyHeaderEnabled = sticky,
                    modifier = Modifier.fillMaxSize()
                )
            }
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