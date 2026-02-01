package com.hien.le.expenseoverview.domain.repository

import com.hien.le.expenseoverview.domain.model.ExpenseItem

interface ExpenseItemRepository {
    suspend fun listByDate(dateIso: String): List<ExpenseItem>
    suspend fun listInRange(fromDateIso: String, toDateIso: String): List<ExpenseItem>
    suspend fun upsert(item: ExpenseItem)
    suspend fun delete(id: String)
}