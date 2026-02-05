package com.hien.le.expenseoverview.presentation.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.export.ReceiptLine
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.ui.components.DateQuickPicker

@Composable
fun SummaryScreen(vm: SummaryViewModel) {
    val state by vm.state.collectAsState()

    val receiptsByDate: Map<String, List<ReceiptLine>> =
        remember(state.receipts) {
            state.receipts.groupBy { it.dateIso }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tổng kết", style = MaterialTheme.typography.headlineSmall)

        // DAY picker
        DateQuickPicker(
            selectedDateIso = state.anchorDateIso,
            onSelectDateIso = { iso -> vm.dispatch(SummaryAction.SelectDay(iso)) }
        )

        // Month + Export + Month dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { vm.dispatch(SummaryAction.SelectCurrentMonth) }) {
                Text("Tháng")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { vm.dispatch(SummaryAction.ExportMonthPdf) },
                enabled = state.mode == SummaryMode.MONTH && !state.isExporting
            ) {
                Text(if (state.isExporting) "Đang export..." else "Export PDF")
            }

            Spacer(Modifier.weight(1f))

            MonthDropdownGerman(
                selectedMonthNumber = state.selectedMonthNumber,
                onMonthSelected = { m -> vm.dispatch(SummaryAction.SelectMonth(m)) }
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (!state.isLoading && state.rows.isEmpty()) {
            Text("Chưa có dữ liệu.")
            return@Column
        }

        // ✅ HEADER ROW (tên cột)
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ngày", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelLarge)
                Text("Bargeld", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Text("Karte", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Text("Hóa đơn", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Text("Net", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            }
        }

        // ✅ LIST
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.rows, key = { it.dateIso }) { r ->
                val receipts = receiptsByDate[r.dateIso].orEmpty()

                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Summary row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = r.dateIso,
                                modifier = Modifier.weight(1.2f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                MoneyFormatter.centsToDeEuro(r.bargeldCents),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                MoneyFormatter.centsToDeEuro(r.karteCents),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                MoneyFormatter.centsToDeEuro(r.expenseCents),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                MoneyFormatter.centsToDeEuro(r.netCents),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Receipt details
                        if (receipts.isNotEmpty()) {
                            Divider()
                            receipts.forEach { rec ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rec.vendorName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = MoneyFormatter.centsToDeEuro(rec.amountCents),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.exportResultMessage != null) {
            AlertDialog(
                onDismissRequest = { vm.dispatch(SummaryAction.ClearExportMessage) },
                title = { Text("Export") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.exportResultMessage!!)
                        if (!state.exportPath.isNullOrBlank()) {
                            Text(state.exportPath!!, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "File nằm trong Documents/ExpenseOverview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
private fun MonthDropdownGerman(
    selectedMonthNumber: Int,
    onMonthSelected: (Int) -> Unit
) {
    val germanMonths = remember {
        listOf(
            "Januar",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember"
        )
    }

    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = germanMonths.getOrNull(selectedMonthNumber - 1) ?: "Monat"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Monat") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().widthIn(min = 180.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            germanMonths.forEachIndexed { index, name ->
                val monthNumber = index + 1
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onMonthSelected(monthNumber)
                        expanded = false
                    }
                )
            }
        }
    }
}