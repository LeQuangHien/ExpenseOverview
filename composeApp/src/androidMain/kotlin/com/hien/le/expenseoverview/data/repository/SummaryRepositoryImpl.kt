package com.hien.le.expenseoverview.data.repository

import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.domain.repository.SummaryRepository
import com.hien.le.expenseoverview.export.ReceiptLine
import com.hien.le.expenseoverview.presentation.summary.SummaryRowUi

class SummaryRepositoryImpl(
    private val dao: ExpenseDao
) : SummaryRepository {

    override suspend fun getSummaryRows(fromDateIso: String, toDateIso: String): List<SummaryRowUi> {
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

    override suspend fun getReceiptsInRange(fromDateIso: String, toDateIso: String): List<ReceiptLine> {
        return dao.listExpenseItemsInRange(fromDateIso, toDateIso).map { it ->
            ReceiptLine(
                dateIso = it.dateIso,
                vendorName = it.vendorName,
                amountCents = it.amountCents
            )
        }
    }
}