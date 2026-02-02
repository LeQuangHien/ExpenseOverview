package com.hien.le.expenseoverview.presentation.summary

import com.hien.le.expenseoverview.domain.model.Summary

enum class SummaryMode { DAY, MONTH }

data class SummaryState(
    val mode: SummaryMode = SummaryMode.MONTH,

    // Anchor date the user picked (for DAY) or inside selected month (for MONTH)
    val anchorDateIso: String = "",

    // month dropdown
    val selectedMonthNumber: Int = 1,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val summary: Summary? = null
)

sealed interface SummaryAction {
    // DAY mode (one specific date)
    data class SelectDay(val dateIso: String) : SummaryAction

    // MONTH mode
    data object SelectCurrentMonth : SummaryAction
    data class SelectMonth(val monthNumber: Int) : SummaryAction

    data object ClearError : SummaryAction
}