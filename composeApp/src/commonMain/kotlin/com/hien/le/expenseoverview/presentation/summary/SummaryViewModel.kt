package com.hien.le.expenseoverview.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.SummaryRange
import com.hien.le.expenseoverview.domain.usecase.GetSummary
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class SummaryViewModel(
    private val getSummary: GetSummary,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    fun dispatch(action: SummaryAction) {
        when (action) {
            is SummaryAction.LoadMonth -> loadMonth(action.anchorDateIso)
            is SummaryAction.ChangeMonth -> changeMonth(action.monthNumber)
            SummaryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadMonth(anchorDateIso: String) {
        val anchor = parseIsoOrToday(anchorDateIso)
        val (from, to) = computeMonthRange(anchor)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    range = SummaryRange.MONTH,
                    anchorDateIso = anchor.toString(),
                    selectedMonthNumber = anchor.monthNumber
                )
            }

            runCatching {
                withContext(dispatchers.io) { getSummary(SummaryRange.MONTH, from, to) }
            }.onSuccess { summary ->
                _state.update { it.copy(isLoading = false, summary = summary) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải tổng kết") }
            }
        }
    }

    private fun changeMonth(monthNumber: Int) {
        val cur = parseIsoOrToday(_state.value.anchorDateIso)
        val clamped = monthNumber.coerceIn(1, 12)

        // giữ nguyên YEAR theo anchor hiện tại, chỉ đổi tháng
        val newAnchor = LocalDate(cur.year, clamped, 1)
        loadMonth(newAnchor.toString())
    }

    private fun computeMonthRange(anchor: LocalDate): Pair<String, String> {
        val first = LocalDate(anchor.year, anchor.monthNumber, 1)
        val firstNext = first + DatePeriod(months = 1)
        val last = firstNext - DatePeriod(days = 1)
        return first.toString() to last.toString()
    }

    private fun parseIsoOrToday(iso: String): LocalDate {
        return runCatching { LocalDate.parse(iso) }
            .getOrElse { LocalDate(2026, 1, 1) } // fallback (bạn có thể đổi thành today nếu muốn)
    }
}