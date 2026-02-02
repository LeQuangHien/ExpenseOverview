package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.export.ReceiptLine

class GetReceiptsInRangeUseCase(
    private val dao: ExpenseDao
) {
    suspend fun execute(fromDateIso: String, toDateIso: String): List<ReceiptLine> {
        return dao.listExpenseItemsInRange(fromDateIso, toDateIso).map { it ->
            ReceiptLine(
                dateIso = it.dateIso,
                vendorName = it.vendorName,
                amountCents = it.amountCents
            )
        }
    }
}