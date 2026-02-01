package com.hien.le.expenseoverview.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable
fun DateQuickPicker(
    selectedDateIso: String,
    onSelectDateIso: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    val todayIso = remember { todayIso() }
    val yesterdayIso = remember { yesterdayIso() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { onSelectDateIso(todayIso) },
            label = { Text("Hôm nay") }
        )
        AssistChip(
            onClick = { onSelectDateIso(yesterdayIso) },
            label = { Text("Hôm qua") }
        )
        OutlinedButton(
            onClick = { showDialog = true }
        ) {
            Text("Chọn ngày")
        }

        Spacer(Modifier.weight(1f))
        Text(selectedDateIso, style = MaterialTheme.typography.labelLarge)
    }

    if (showDialog) {
        ManualDateDialog(
            initialIso = selectedDateIso,
            onDismiss = { showDialog = false },
            onConfirm = { iso ->
                onSelectDateIso(iso)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ManualDateDialog(
    initialIso: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialIso) }
    val error = remember(text) { validateIsoDate(text) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nhập ngày") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Ví dụ: 2026-01-31", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = error == null
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}

private fun validateIsoDate(iso: String): String? {
    return try {
        LocalDate.parse(iso)
        null
    } catch (_: Throwable) {
        "Ngày không hợp lệ"
    }
}

private fun todayIso(): String =
    Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

private fun yesterdayIso(): String =
    (Clock.System.todayIn(TimeZone.currentSystemDefault()) - DatePeriod(days = 1)).toString()
