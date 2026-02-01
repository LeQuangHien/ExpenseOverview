package com.hien.le.expenseoverview.data.repository

import com.hien.le.expenseoverview.data.mapper.toDomain
import com.hien.le.expenseoverview.data.mapper.toEntity
import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.domain.model.ExpenseItem
import com.hien.le.expenseoverview.domain.repository.ExpenseItemRepository

class ExpenseItemRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseItemRepository {

    override suspend fun listByDate(dateIso: String): List<ExpenseItem> =
        dao.listExpenseItemsByDate(dateIso).map { it.toDomain() }

    override suspend fun listInRange(fromDateIso: String, toDateIso: String): List<ExpenseItem> =
        dao.listExpenseItemsInRange(fromDateIso, toDateIso).map { it.toDomain() }

    override suspend fun upsert(item: ExpenseItem) {
        dao.upsertExpenseItem(item.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteExpenseItem(id)
    }
}