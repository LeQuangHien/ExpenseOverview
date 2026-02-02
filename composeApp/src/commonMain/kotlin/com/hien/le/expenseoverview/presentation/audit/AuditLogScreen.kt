package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.domain.model.AuditFields
import com.hien.le.expenseoverview.presentation.audit.AuditLogViewModel
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AuditLogScreen(vm: AuditLogViewModel) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Log", style = MaterialTheme.typography.headlineSmall)

            TextButton(
                onClick = { vm.refresh() },
                enabled = !state.isLoading
            ) {
                Text("Refresh")
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        val events = state.events
        if (events.isEmpty()) {
            Text("Chưa có chỉnh sửa nào.")
            return@Column
        }

        val tz = TimeZone.currentSystemDefault()

        val grouped = events.groupBy { it.entityDateIso }
        val sortedDates = grouped.keys.sortedDescending()

        sortedDates.forEach { dateIso ->
            val list = grouped[dateIso].orEmpty()

            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ngày: $dateIso", style = MaterialTheme.typography.titleMedium)

                    list.sortedByDescending { it.editedAt }.forEach { e ->
                        val line = when (e.field) {
                            AuditFields.BARGELD ->
                                "Bargeld: ${MoneyFormatter.centsToDeEuro(e.oldValue.toLong())} → ${MoneyFormatter.centsToDeEuro(e.newValue.toLong())}"

                            AuditFields.KARTE ->
                                "Karte: ${MoneyFormatter.centsToDeEuro(e.oldValue.toLong())} → ${MoneyFormatter.centsToDeEuro(e.newValue.toLong())}"

                            AuditFields.EXPENSE_TOTAL ->
                                "Chi tiêu (tổng): ${MoneyFormatter.centsToDeEuro(e.oldValue.toLong())} → ${MoneyFormatter.centsToDeEuro(e.newValue.toLong())}"

                            else ->
                                "${e.field}: ${e.oldValue} → ${e.newValue}"
                        }

                        val editedTimeText = formatEditedAtDe(e.editedAt, tz)

                        // ✅ show time
                        Text(
                            text = editedTimeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(line, style = MaterialTheme.typography.bodyMedium)

                        if (!e.comment.isNullOrBlank()) {
                            Text(
                                text = "Ghi chú: ${e.comment}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Divider()
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun formatEditedAtDe(epochMs: Long, tz: TimeZone): String {
    val dt = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(tz)
    // dd.MM.yyyy HH:mm
    val dd = dt.dayOfMonth.toString().padStart(2, '0')
    val mm = dt.monthNumber.toString().padStart(2, '0')
    val yyyy = dt.year.toString()
    val hh = dt.hour.toString().padStart(2, '0')
    val min = dt.minute.toString().padStart(2, '0')
    return "$dd.$mm.$yyyy $hh:$min"
}