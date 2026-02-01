package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.repository.ExpenseItemRepository

class DeleteExpenseItem(private val repo: ExpenseItemRepository) {
    suspend operator fun invoke(id: String) = repo.delete(id)
}