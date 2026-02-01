package com.hien.le.expenseoverview.domain.model

data class ExpenseItem(
    val id: String,
    val dateIso: String,
    val vendorName: String,
    val amount: Cents,
    val createdAt: Long
)