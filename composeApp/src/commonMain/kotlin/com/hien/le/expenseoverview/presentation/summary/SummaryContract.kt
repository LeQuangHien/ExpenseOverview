package com.hien.le.expenseoverview.presentation.summary

import com.hien.le.expenseoverview.domain.model.Summary
import com.hien.le.expenseoverview.domain.model.SummaryRange

data class SummaryState(
    val range: SummaryRange = SummaryRange.DAY,
    val anchorDateIso: String = "1970-01-01",
    val fromDateIso: String = "1970-01-01",
    val toDateIso: String = "1970-01-01",
    val isLoading: Boolean = false,
    val summary: Summary? = null,
    val errorMessage: String? = null
)

sealed interface SummaryAction {
    data class Load(val range: SummaryRange, val anchorDateIso: String) : SummaryAction
    data object ClearError : SummaryAction
}

sealed interface SummaryEffect {
    data class Toast(val message: String) : SummaryEffect
}