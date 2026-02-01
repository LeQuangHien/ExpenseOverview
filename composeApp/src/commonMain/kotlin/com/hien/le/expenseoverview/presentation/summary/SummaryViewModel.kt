package com.hien.le.expenseoverview.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.SummaryRange
import com.hien.le.expenseoverview.domain.usecase.GetSummary
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryViewModel(
    private val getSummary: GetSummary,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(SummaryState())
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    fun dispatch(action: SummaryAction) {
        when (action) {
            is SummaryAction.Load -> load(action.range, action.anchorDateIso)
            SummaryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(range: SummaryRange, anchor: String) {
        val (from, to) = computeRange(range, anchor)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    range = range,
                    anchorDateIso = anchor,
                    fromDateIso = from,
                    toDateIso = to
                )
            }

            runCatching {
                withContext(dispatchers.io) { getSummary(range, from, to) }
            }.onSuccess { summary ->
                _state.update { it.copy(isLoading = false, summary = summary) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải tổng kết") }
            }
        }
    }

    private fun computeRange(range: SummaryRange, anchorIso: String): Pair<String, String> {
        // TODO: bạn nâng cấp bằng kotlinx-datetime (tuần/tháng)
        return when (range) {
            SummaryRange.DAY -> anchorIso to anchorIso
            SummaryRange.WEEK -> anchorIso to anchorIso
            SummaryRange.MONTH -> anchorIso to anchorIso
        }
    }
}