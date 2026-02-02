package com.hien.le.expenseoverview.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.SummaryRange
import com.hien.le.expenseoverview.domain.usecase.GetSummary
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class SummaryViewModel(
    private val getSummary: GetSummary,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val tz = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        run {
            val today = Clock.System.todayIn(tz)
            SummaryState(
                mode = SummaryMode.MONTH,
                anchorDateIso = today.toString(),
                selectedMonthNumber = today.monthNumber
            )
        }
    )
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    fun dispatch(action: SummaryAction) {
        when (action) {
            is SummaryAction.SelectDay -> loadDay(action.dateIso)
            SummaryAction.SelectCurrentMonth -> loadMonth(Clock.System.todayIn(tz))
            is SummaryAction.SelectMonth -> changeMonth(action.monthNumber)
            SummaryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadDay(dateIso: String) {
        val date = parseIsoOrToday(dateIso)
        val fromIso = date.toString()
        val toIso = date.toString()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    mode = SummaryMode.DAY,
                    isLoading = true,
                    errorMessage = null,
                    anchorDateIso = date.toString(),
                    // dropdown vẫn show month của ngày đó (để consistent UI)
                    selectedMonthNumber = date.monthNumber
                )
            }

            runCatching {
                withContext(dispatchers.io) {
                    getSummary(SummaryRange.DAY, fromIso, toIso)
                }
            }.onSuccess { summary ->
                _state.update { it.copy(isLoading = false, summary = summary) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải tổng kết") }
            }
        }
    }

    private fun loadMonth(monthAnchor: LocalDate) {
        val (fromIso, toIso) = computeMonthRange(monthAnchor)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    mode = SummaryMode.MONTH,
                    isLoading = true,
                    errorMessage = null,
                    anchorDateIso = monthAnchor.toString(),
                    selectedMonthNumber = monthAnchor.monthNumber
                )
            }

            runCatching {
                withContext(dispatchers.io) {
                    getSummary(SummaryRange.MONTH, fromIso, toIso)
                }
            }.onSuccess { summary ->
                _state.update { it.copy(isLoading = false, summary = summary) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải tổng kết") }
            }
        }
    }

    private fun changeMonth(monthNumber: Int) {
        val current = parseIsoOrToday(_state.value.anchorDateIso)
        val m = monthNumber.coerceIn(1, 12)

        // ✅ Không show năm trong dropdown, nhưng vẫn cần year để query.
        // Mình dùng year hiện tại theo anchorDateIso (thường là năm hiện tại).
        val newAnchor = LocalDate(current.year, m, 1)
        loadMonth(newAnchor)
    }

    private fun computeMonthRange(anchor: LocalDate): Pair<String, String> {
        val first = LocalDate(anchor.year, anchor.monthNumber, 1)
        val firstNext = first + DatePeriod(months = 1)
        val last = firstNext - DatePeriod(days = 1)
        return first.toString() to last.toString()
    }

    private fun parseIsoOrToday(iso: String): LocalDate =
        runCatching { LocalDate.parse(iso) }
            .getOrElse { Clock.System.todayIn(tz) }
}