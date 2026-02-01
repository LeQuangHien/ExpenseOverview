package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.ExpenseItem
import com.hien.le.expenseoverview.domain.repository.ExpenseItemRepository

class GetExpenseItemsByDate(private val repo: ExpenseItemRepository) {
    suspend operator fun invoke(dateIso: String): List<ExpenseItem> = repo.listByDate(dateIso)
}