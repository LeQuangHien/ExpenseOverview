package com.hien.le.expenseoverview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.domain.model.SummaryRow
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

@Composable
fun SummaryTable(
    rows: List<SummaryRowUi>,
    stickyHeaderEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (stickyHeaderEnabled) {
                stickyHeader {
                    HeaderRow()
                    Divider()
                }
            } else {
                item {
                    HeaderRow()
                    Divider()
                }
            }

            items(rows, key = { it.dateIso }) { r ->
                DataRow(r)
                Divider()
            }
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
private fun DataRow(r: SummaryRowUi) {
    Row(Modifier.fillMaxWidth()) {
        Cell(r.dateIso, Modifier.weight(1.2f))
        Cell(MoneyFormatter.centsToDeEuro(r.bargeldCents), Modifier.weight(1f))
        Cell(MoneyFormatter.centsToDeEuro(r.karteCents), Modifier.weight(1f))
        Cell(MoneyFormatter.centsToDeEuro(r.expenseCents), Modifier.weight(1f))
        Cell(MoneyFormatter.centsToDeEuro(r.netCents), Modifier.weight(1f))
    }
}

@Composable
private fun Cell(text: String, modifier: Modifier, isHeader: Boolean = false) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 6.dp, horizontal = 4.dp),
        style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium
    )
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