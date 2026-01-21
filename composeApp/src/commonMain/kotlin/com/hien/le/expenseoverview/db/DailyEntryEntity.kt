package com.hien.le.expenseoverview.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entry")
data class DailyEntryEntity(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val bargeldCents: Long,
    val karteCents: Long,
    val expenseCents: Long,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)