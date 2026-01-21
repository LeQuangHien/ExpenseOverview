package com.hien.le.expenseoverview.data.repository

import com.hien.le.expenseoverview.data.mapper.toDomain
import com.hien.le.expenseoverview.data.mapper.toEntity
import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.repository.EntryRepository

class EntryRepositoryImpl(
    private val dao: ExpenseDao
) : EntryRepository {

    override suspend fun getByDate(dateIso: String): DailyEntry? =
        dao.getEntryByDate(dateIso)?.toDomain()

    override suspend fun upsert(entry: DailyEntry) {
        dao.upsertEntry(entry.toEntity())
    }

    override suspend fun listInRange(fromDateIso: String, toDateIso: String): List<DailyEntry> =
        dao.listEntriesInRange(fromDateIso, toDateIso).map { it.toDomain() }
}