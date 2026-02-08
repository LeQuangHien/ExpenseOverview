package com.hien.le.expenseoverview.presentation.entry

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

    // Mặc định là hôm nay khi mở lần đầu
    val todayIso = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    LaunchedEffect(Unit) {
        if (state.dateIso.isBlank() || state.dateIso == "1970-01-01") {
            vm.dispatch(EntryAction.ChangeDate(todayIso))
        }
    }

    // Focus lại ô "Tiền mặt" sau khi lưu thành công
    val cashFocus = remember { FocusRequester() }
    var pendingFocusCash by remember { mutableStateOf(false) }

    // Dialog state
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Khi có lỗi -> mở dialog
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) showErrorDialog = true
    }

    // Lắng nghe effect: lưu thành công -> dialog + focus lại
    LaunchedEffect(vm) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                EntryEffect.SaveSuccess -> {
                    showSuccessDialog = true
                    pendingFocusCash = true
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
            onDismissRequest = { /* bắt buộc bấm OK */ },
            title = { Text("Thành công") },
            text = { Text("Đã lưu thành công.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    if (pendingFocusCash) {
                        pendingFocusCash = false
                        cashFocus.requestFocus()
                    }
                }) { Text("OK") }
            }
        )
    }

    // Nút lưu chỉ bật khi cả 2 ô đã nhập + hợp lệ
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
        Text("Nhập liệu trong ngày", style = MaterialTheme.typography.headlineSmall)

        DateQuickPicker(
            selectedDateIso = state.dateIso,
            onSelectDateIso = { vm.dispatch(EntryAction.ChangeDate(it)) }
        )

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Tiền mặt – xanh lá
            MoneyKeypadInput(
                label = "Tiền mặt",
                text = state.bargeldText,
                onTextChange = { vm.dispatch(EntryAction.EditBargeld(it)) },
                keyColor = MaterialTheme.colorScheme.secondary
            )

            // Thẻ – xanh dương
            MoneyKeypadInput(
                label = "Thẻ",
                text = state.karteText,
                onTextChange = { vm.dispatch(EntryAction.EditKarte(it)) },
                keyColor = MaterialTheme.colorScheme.primary
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

        // Hóa đơn – cam
        MoneyKeypadInput(
            label = "Số tiền mua",
            text = state.expenseAmountText,
            onTextChange = { vm.dispatch(EntryAction.EditExpenseAmount(it)) },
            keyColor = MaterialTheme.colorScheme.tertiary
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
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                Text("Lợi nhuận: ${MoneyFormatter.centsToDeEuro(state.netCents)}")
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