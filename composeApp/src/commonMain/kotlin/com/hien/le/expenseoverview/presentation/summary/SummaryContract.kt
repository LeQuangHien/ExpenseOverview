package com.hien.le.expenseoverview.presentation.summary

import com.hien.le.expenseoverview.domain.model.Summary
import com.hien.le.expenseoverview.domain.model.SummaryRange

data class SummaryState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val range: SummaryRange = SummaryRange.MONTH,
    val anchorDateIso: String = "1970-01-01",

    // để dropdown hiển thị tháng đang chọn (1..12)
    val selectedMonthNumber: Int = 1,

    val summary: com.hien.le.expenseoverview.domain.model.Summary? = null
)

sealed interface SummaryAction {
    data class LoadMonth(val anchorDateIso: String) : SummaryAction
    data class ChangeMonth(val monthNumber: Int) : SummaryAction
    data object ClearError : SummaryAction
}

sealed interface SummaryEffect {
    data class Toast(val message: String) : SummaryEffect
}