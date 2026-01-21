package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.repository.EntryRepository

class GetDailyEntry(private val repo: EntryRepository) {
    suspend operator fun invoke(dateIso: String): DailyEntry? = repo.getByDate(dateIso)
}