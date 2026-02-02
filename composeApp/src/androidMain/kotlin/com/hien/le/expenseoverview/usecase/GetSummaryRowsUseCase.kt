package com.hien.le.expenseoverview.usecase

import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

class GetSummaryRowsUseCase(
    private val dao: ExpenseDao
) {
    suspend fun execute(fromDateIso: String, toDateIso: String): List<SummaryRowUi> {
        val entries = dao.listEntriesInRange(fromDateIso, toDateIso)
        val items = dao.listExpenseItemsInRange(fromDateIso, toDateIso)

        val expenseSumByDate: Map<String, Long> =
            items.groupBy { it.dateIso }
                .mapValues { (_, list) -> list.sumOf { it.amountCents } }

        return entries.map { e ->
            val expense = expenseSumByDate[e.dateIso] ?: 0L
            val revenue = e.bargeldCents + e.karteCents
            SummaryRowUi(
                dateIso = e.dateIso,
                bargeldCents = e.bargeldCents,
                karteCents = e.karteCents,
                expenseCents = expense,
                netCents = revenue - expense
            )
        }
    }
}