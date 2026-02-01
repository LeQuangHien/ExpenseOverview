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
import com.hien.le.expenseoverview.ui.components.DateQuickPicker
import com.hien.le.expenseoverview.ui.components.MoneyKeypadInput

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
            onSelectDateIso = { iso ->
                vm.dispatch(EntryAction.ChangeDate(iso))
            }
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val wide = maxWidth >= 600.dp

            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MoneyKeypadInput(
                            label = "Chi tiêu",
                            text = state.expenseText,
                            onTextChange = { vm.dispatch(EntryAction.EditExpense(it)) }
                        )

                        OutlinedTextField(
                            value = state.noteText,
                            onValueChange = { vm.dispatch(EntryAction.EditNote(it)) },
                            label = { Text("Ghi chú (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                    MoneyKeypadInput(
                        label = "Chi tiêu",
                        text = state.expenseText,
                        onTextChange = { vm.dispatch(EntryAction.EditExpense(it)) }
                    )

                    OutlinedTextField(
                        value = state.noteText,
                        onValueChange = { vm.dispatch(EntryAction.EditNote(it)) },
                        label = { Text("Ghi chú (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Tổng doanh thu: ${MoneyFormatter.centsToDeEuro(state.totalRevenueCents)}")
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
            Text(if (state.isLoading) "Đang lưu..." else "Lưu")
        }

        Spacer(Modifier.height(24.dp))
    }
}