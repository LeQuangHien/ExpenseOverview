package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.entry.EntryAction
import com.hien.le.expenseoverview.presentation.entry.EntryEffect
import com.hien.le.expenseoverview.presentation.entry.EntryViewModel
import com.hien.le.expenseoverview.presentation.entry.VendorPreset
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.MoneyKeypadInput
import com.hien.le.expenseoverview.ui.components.VendorDropdown
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun EntryScreen(vm: EntryViewModel) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    // Default today on first open
    val todayIso = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    LaunchedEffect(Unit) {
        if (state.dateIso.isBlank() || state.dateIso == "1970-01-01") {
            vm.dispatch(EntryAction.ChangeDate(todayIso))
        }
    }

    // Focus Bargeld after save success
    val bargeldFocus = remember { FocusRequester() }
    var pendingFocusBargeld by remember { mutableStateOf(false) }

    // Dialog state
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Error dialog triggers when errorMessage becomes non-null
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) showErrorDialog = true
    }

    // Listen to effects: Save success -> show dialog, then focus after OK
    LaunchedEffect(vm) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                EntryEffect.SaveSuccess -> {
                    showSuccessDialog = true
                    pendingFocusBargeld = true
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Lỗi") },
            text = { Text("Đã có lỗi xảy ra. Vui lòng thử lại.") },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    vm.dispatch(EntryAction.ClearError)
                }) { Text("OK") }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* force user tap OK */ },
            title = { Text("Thành công") },
            text = { Text("Đã lưu thành công.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    // ✅ focus lại Bargeld sau khi đóng dialog
                    if (pendingFocusBargeld) {
                        pendingFocusBargeld = false
                        bargeldFocus.requestFocus()
                    }
                }) { Text("OK") }
            }
        )
    }

    // Save enabled: BOTH fields filled + state.canSave + !loading
    val saveEnabled = remember(state.bargeldText, state.karteText, state.canSave, state.isLoading) {
        state.bargeldText.trim().isNotEmpty() &&
                state.karteText.trim().isNotEmpty() &&
                state.canSave &&
                !state.isLoading
    }

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

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ✅ auto focus target
            MoneyKeypadInput(
                label = "Bargeld",
                text = state.bargeldText,
                onTextChange = { vm.dispatch(EntryAction.EditBargeld(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(bargeldFocus)
            )

            MoneyKeypadInput(
                label = "Karte",
                text = state.karteText,
                onTextChange = { vm.dispatch(EntryAction.EditKarte(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.noteText,
                onValueChange = { vm.dispatch(EntryAction.EditNote(it)) },
                label = { Text("Ghi chú (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

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
            onTextChange = { vm.dispatch(EntryAction.EditExpenseAmount(it)) },
            modifier = Modifier.fillMaxWidth()
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
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Danh sách hóa đơn", style = MaterialTheme.typography.titleSmall)

                    state.expenseItems.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(item.vendorName, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    MoneyFormatter.centsToDeEuro(item.amountCents),
                                    style = MaterialTheme.typography.bodySmall
                                )
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

        Card {
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Tổng doanh thu: ${MoneyFormatter.centsToDeEuro(state.totalRevenueCents)}")
                Text("Tổng chi tiêu: ${MoneyFormatter.centsToDeEuro(state.totalExpenseCents)}")
                Text("Net: ${MoneyFormatter.centsToDeEuro(state.netCents)}")
            }
        }

        Button(
            onClick = { vm.dispatch(EntryAction.Save()) },
            enabled = saveEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Đang lưu..." else "Lưu ngày")
        }

        Spacer(Modifier.height(24.dp))
    }
}