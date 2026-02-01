package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.model.ExpenseItem
import com.hien.le.expenseoverview.domain.repository.ExpenseItemRepository
import com.hien.le.expenseoverview.platform.Clock
import kotlin.random.Random

class AddExpenseItem(
    private val repo: ExpenseItemRepository,
    private val clock: Clock
) {
    data class Input(
        val dateIso: String,
        val vendorName: String,
        val amount: Cents
    )

    suspend operator fun invoke(input: Input): ExpenseItem {
        val now = clock.nowEpochMillis()
        val id = "ex_${now}_${Random.nextInt(100000, 999999)}"
        val item = ExpenseItem(
            id = id,
            dateIso = input.dateIso,
            vendorName = input.vendorName.trim(),
            amount = input.amount,
            createdAt = now
        )
        repo.upsert(item)
        return item
    }
}