package com.hien.le.expenseoverview.data.mapper

import com.hien.le.expenseoverview.db.ExpenseItemEntity
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.model.ExpenseItem

fun ExpenseItemEntity.toDomain(): ExpenseItem =
    ExpenseItem(
        id = id,
        dateIso = dateIso,
        vendorName = vendorName,
        amount = Cents(amountCents),
        createdAt = createdAt
    )

fun ExpenseItem.toEntity(): ExpenseItemEntity =
    ExpenseItemEntity(
        id = id,
        dateIso = dateIso,
        vendorName = vendorName,
        amountCents = amount.value,
        createdAt = createdAt
    )