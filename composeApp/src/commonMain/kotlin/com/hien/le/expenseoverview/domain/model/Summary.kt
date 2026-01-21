package com.hien.le.expenseoverview.domain.model

enum class SummaryRange { DAY, WEEK, MONTH }

data class SummaryRow(
    val dateIso: String,
    val bargeld: Cents,
    val karte: Cents,
    val expense: Cents
) {
    val revenue: Long get() = bargeld.value + karte.value
    val net: Long get() = revenue - expense.value
}

data class Summary(
    val range: SummaryRange,
    val fromDateIso: String,
    val toDateIso: String,
    val rows: List<SummaryRow>
) {
    val totalBargeld: Long = rows.sumOf { it.bargeld.value }
    val totalKarte: Long = rows.sumOf { it.karte.value }
    val totalExpense: Long = rows.sumOf { it.expense.value }
    val totalRevenue: Long = totalBargeld + totalKarte
    val totalNet: Long = totalRevenue - totalExpense
}