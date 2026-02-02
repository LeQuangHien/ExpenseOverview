package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.summary.SummaryAction
import com.hien.le.expenseoverview.presentation.summary.SummaryViewModel
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.SummaryTable
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun SummaryScreen(vm: SummaryViewModel) {
    val state by vm.state.collectAsState()

    // Default: tháng hiện tại
    val todayIso = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    LaunchedEffect(Unit) {
        if (state.anchorDateIso.isBlank() || state.anchorDateIso == "1970-01-01") {
            vm.dispatch(SummaryAction.LoadMonth(anchorDateIso = todayIso))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tổng kết", style = MaterialTheme.typography.headlineSmall)

        // Hôm nay/Hôm qua/Chọn ngày -> load theo tháng của ngày đó
        DateQuickPicker(
            selectedDateIso = state.anchorDateIso.ifBlank { todayIso },
            onSelectDateIso = { iso -> vm.dispatch(SummaryAction.LoadMonth(anchorDateIso = iso)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Nút Tháng: bấm vào -> tháng hiện tại
            Button(onClick = { vm.dispatch(SummaryAction.LoadMonth(anchorDateIso = todayIso)) }) {
                Text("Tháng")
            }

            Spacer(Modifier.weight(1f))

            // ✅ Dropdown chọn tháng 1..12 (không cần năm)
            MonthDropdown(
                selectedMonthNumber = state.selectedMonthNumber,
                onMonthSelected = { month ->
                    vm.dispatch(SummaryAction.ChangeMonth(month))
                }
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        val summary = state.summary
        if (summary == null) {
            Text("Chưa có dữ liệu.")
            if (state.errorMessage != null) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            return
        }

        // Table (sticky header trên tablet)
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