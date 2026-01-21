package com.hien.le.expenseoverview.domain.repository

import com.hien.le.expenseoverview.domain.model.DailyEntry

interface EntryRepository {
    suspend fun getByDate(dateIso: String): DailyEntry?
    suspend fun upsert(entry: DailyEntry)
    suspend fun listInRange(fromDateIso: String, toDateIso: String): List<DailyEntry>
}