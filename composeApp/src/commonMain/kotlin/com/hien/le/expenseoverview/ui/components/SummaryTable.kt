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

@Composable
fun SummaryTable(
    rows: List<SummaryRow>,
    stickyHeaderEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (stickyHeaderEnabled) {
            stickyHeader {
                HeaderRow()
            }
        } else {
            item { HeaderRow() }
        }

        items(rows) { r ->
            DataRow(r)
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
private fun DataRow(r: SummaryRow) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp)) {
        BodyCell(r.dateIso, 1.2f)
        BodyCell(MoneyFormatter.centsToDeEuro(r.bargeld.value), 1f)
        BodyCell(MoneyFormatter.centsToDeEuro(r.karte.value), 1f)
        BodyCell(MoneyFormatter.centsToDeEuro(r.expense.value), 1f)
        BodyCell(MoneyFormatter.centsToDeEuro(r.net), 1f)
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