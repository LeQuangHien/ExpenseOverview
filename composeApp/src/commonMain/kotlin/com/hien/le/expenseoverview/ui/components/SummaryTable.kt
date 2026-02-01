package com.hien.le.expenseoverview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.domain.model.SummaryRow
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter

@Composable
fun SummaryTable(
    rows: List<SummaryRow>,
    stickyHeaderEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expandedDate by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (stickyHeaderEnabled) {
            stickyHeader { HeaderRow() }
        } else {
            item { HeaderRow() }
        }

        itemsIndexed(rows) { _, r ->
            DayRow(
                row = r,
                expanded = expandedDate == r.dateIso,
                onToggle = {
                    expandedDate = if (expandedDate == r.dateIso) null else r.dateIso
                }
            )
            Divider()
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        HeaderCell("Ngày", 1.2f)
        HeaderCell("Bargeld", 1f)
        HeaderCell("Karte", 1f)
        HeaderCell("Expense", 1f)
        HeaderCell("Net", 1f)
    }
    Divider()
}

@Composable
private fun DayRow(row: SummaryRow, expanded: Boolean, onToggle: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BodyCell(row.dateIso, 1.2f)
            BodyCell(MoneyFormatter.centsToDeEuro(row.bargeld.value), 1f)
            BodyCell(MoneyFormatter.centsToDeEuro(row.karte.value), 1f)
            BodyCell(MoneyFormatter.centsToDeEuro(row.expenseTotal.value), 1f)
            BodyCell(MoneyFormatter.centsToDeEuro(row.net), 1f)
        }

        TextButton(onClick = onToggle) {
            Text(if (expanded) "Ẩn hóa đơn" else "Xem hóa đơn (${row.receipts.size})")
        }

        if (expanded) {
            if (row.receipts.isEmpty()) {
                Text("Không có hóa đơn.", style = MaterialTheme.typography.bodySmall)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.receipts.forEach { it ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(it.vendorName, modifier = Modifier.weight(1f))
                            Text(MoneyFormatter.centsToDeEuro(it.amount.value))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(text, modifier = Modifier.weight(weight), style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun RowScope.BodyCell(text: String, weight: Float) {
    Text(text, modifier = Modifier.weight(weight), style = MaterialTheme.typography.bodyMedium)
}