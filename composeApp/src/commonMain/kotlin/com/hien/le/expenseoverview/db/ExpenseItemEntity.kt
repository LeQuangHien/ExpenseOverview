package com.hien.le.expenseoverview.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_item",
    indices = [
        Index(value = ["dateIso"]),
        Index(value = ["createdAt"])
    ]
)
data class ExpenseItemEntity(
    @PrimaryKey val id: String,
    val dateIso: String,        // YYYY-MM-DD
    val vendorName: String,     // "Aldi Süd", "Rewe", ...
    val amountCents: Long,      // >= 0
    val createdAt: Long
)