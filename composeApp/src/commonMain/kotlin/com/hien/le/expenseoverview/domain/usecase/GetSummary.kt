package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.model.Summary
import com.hien.le.expenseoverview.domain.model.SummaryRange
import com.hien.le.expenseoverview.domain.model.SummaryRow
import com.hien.le.expenseoverview.domain.repository.EntryRepository

class GetSummary(private val repo: EntryRepository) {
    suspend operator fun invoke(range: SummaryRange, fromDateIso: String, toDateIso: String): Summary {
        val entries = repo.listInRange(fromDateIso, toDateIso)
        val rows = entries.map {
            SummaryRow(
                dateIso = it.dateIso,
                bargeld = Cents(it.bargeld.value),
                karte = Cents(it.karte.value),
                expense = Cents(it.expense.value)
            )
        }
        return Summary(range, fromDateIso, toDateIso, rows)
    }
}