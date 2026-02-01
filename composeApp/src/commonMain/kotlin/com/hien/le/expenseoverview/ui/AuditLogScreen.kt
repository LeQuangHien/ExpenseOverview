package com.hien.le.expenseoverview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.audit.AuditAction
import com.hien.le.expenseoverview.presentation.audit.AuditLogViewModel

@Composable
fun AuditLogScreen(vm: AuditLogViewModel) {
    val state by vm.state.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Audit log", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.dispatch(AuditAction.PurgeOld) }) {
                Text("Dọn log > 1 năm")
            }
            // Bạn có thể thêm filter theo date/range ở đây
        }

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.events) { e ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${e.entityDateIso} • ${e.field}", style = MaterialTheme.typography.titleSmall)
                        Text("${e.oldValue} → ${e.newValue}")
                        Text("at: ${e.editedAt}")
                        if (!e.comment.isNullOrBlank()) Text("note: ${e.comment}")
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}