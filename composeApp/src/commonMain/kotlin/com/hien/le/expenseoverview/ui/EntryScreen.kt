package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.entry.EntryAction
import com.hien.le.expenseoverview.presentation.entry.EntryViewModel
import com.hien.le.expenseoverview.presentation.entry.VendorPreset
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.MoneyKeypadInput
import com.hien.le.expenseoverview.ui.components.VendorDropdown

@Composable
fun EntryScreen(vm: EntryViewModel) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nhập số", style = MaterialTheme.typography.headlineSmall)

        DateQuickPicker(
            selectedDateIso = state.dateIso,
            onSelectDateIso = { vm.dispatch(EntryAction.ChangeDate(it)) }
        )

        // Revenue inputs
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyKeypadInput(
                    label = "Bargeld",
                    text = state.bargeldText,
                    onTextChange = { vm.dispatch(EntryAction.EditBargeld(it)) }
                )
                MoneyKeypadInput(
                    label = "Karte",
                    text = state.karteText,
                    onTextChange = { vm.dispatch(EntryAction.EditKarte(it)) }
                )
                OutlinedTextField(
                    value = state.noteText,
                    onValueChange = { vm.dispatch(EntryAction.EditNote(it)) },
                    label = { Text("Ghi chú (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Expense items section
        Text("Chi tiêu (hóa đơn)", style = MaterialTheme.typography.titleMedium)

        VendorDropdown(
            selected = state.vendorPreset,
            onSelect = { vm.dispatch(EntryAction.SelectVendor(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.vendorPreset == VendorPreset.OTHER) {
            OutlinedTextField(
                value = state.vendorCustomText,
                onValueChange = { vm.dispatch(EntryAction.EditVendorCustom(it)) },
                label = { Text("Nhập tên nơi mua") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        MoneyKeypadInput(
            label = "Số tiền mua",
            text = state.expenseAmountText,
            onTextChange = { vm.dispatch(EntryAction.EditExpenseAmount(it)) }
        )

        Button(
            onClick = { vm.dispatch(EntryAction.AddExpenseItem) },
            enabled = state.canAddExpense && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm hóa đơn")
        }

        if (state.expenseItems.isNotEmpty()) {
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Danh sách hóa đơn", style = MaterialTheme.typography.titleSmall)

                    state.expenseItems.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(item.vendorName, style = MaterialTheme.typography.bodyMedium)
                                Text(MoneyFormatter.centsToDeEuro(item.amountCents), style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { vm.dispatch(EntryAction.DeleteExpenseItem(item.id)) }) {
                                Text("Xóa")
                            }
                        }
                        Divider()
                    }
                }
            }
        }

        // Totals
        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tổng doanh thu: ${MoneyFormatter.centsToDeEuro(state.totalRevenueCents)}")
                Text("Tổng chi tiêu: ${MoneyFormatter.centsToDeEuro(state.totalExpenseCents)}")
                Text("Net: ${MoneyFormatter.centsToDeEuro(state.netCents)}")
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { vm.dispatch(EntryAction.Save()) },
            enabled = state.canSave && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Đang lưu..." else "Lưu ngày")
        }

        Spacer(Modifier.height(24.dp))
    }
}