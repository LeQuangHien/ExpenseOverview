package com.hien.le.expenseoverview.domain.model

data class DailyEntry(
    val dateIso: String,
    val bargeld: Cents,
    val karte: Cents,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val revenueCents: Long get() = bargeld.value + karte.value
}