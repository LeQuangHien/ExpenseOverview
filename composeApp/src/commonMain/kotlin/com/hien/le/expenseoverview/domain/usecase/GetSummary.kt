package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.*
import com.hien.le.expenseoverview.domain.repository.EntryRepository
import com.hien.le.expenseoverview.domain.repository.ExpenseItemRepository

class GetSummary(
    private val entryRepo: EntryRepository,
    private val expenseRepo: ExpenseItemRepository
) {
    suspend operator fun invoke(range: SummaryRange, fromDateIso: String, toDateIso: String): Summary {
        val entries = entryRepo.listInRange(fromDateIso, toDateIso)
        val allReceipts = expenseRepo.listInRange(fromDateIso, toDateIso)
            .groupBy { it.dateIso }

        val rows = entries.map { e ->
            val receipts = allReceipts[e.dateIso].orEmpty()
            val expenseTotal = receipts.sumOf { it.amount.value }

            SummaryRow(
                dateIso = e.dateIso,
                bargeld = e.bargeld,
                karte = e.karte,
                expenseTotal = Cents(expenseTotal),
                receipts = receipts
            )
        }

        return Summary(range, fromDateIso, toDateIso, rows)
    }
}