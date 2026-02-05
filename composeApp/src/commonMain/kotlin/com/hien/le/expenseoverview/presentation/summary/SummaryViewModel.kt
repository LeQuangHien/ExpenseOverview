package com.hien.le.expenseoverview.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.repository.SummaryRepository
import com.hien.le.expenseoverview.export.MonthPdfExporter
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class SummaryViewModel(
    private val repo: SummaryRepository,
    private val pdfExporter: MonthPdfExporter,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val tz = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        run {
            val today = Clock.System.todayIn(tz)
            SummaryState(
                mode = SummaryMode.MONTH,
                anchorDateIso = today.toString(),
                selectedMonthNumber = today.monthNumber,
                receipts = emptyList() // ✅ ensure typed
            )
        }
    )
    val state: StateFlow<SummaryState> = _state.asStateFlow()

    init {
        dispatch(SummaryAction.SelectCurrentMonth)
    }

    fun dispatch(action: SummaryAction) {
        when (action) {
            is SummaryAction.SelectDay -> loadDay(action.dateIso)
            SummaryAction.SelectCurrentMonth -> loadMonth(Clock.System.todayIn(tz))
            is SummaryAction.SelectMonth -> changeMonth(action.monthNumber)

            SummaryAction.ExportMonthPdf -> exportMonthPdf()
            SummaryAction.ClearExportMessage ->
                _state.update { it.copy(exportResultMessage = null, exportPath = null, isExporting = false) }

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
                    anchorDateIso = fromIso,
                    selectedMonthNumber = date.monthNumber,
                    rows = emptyList(),
                    receipts = emptyList()
                )
            }

            runCatching {
                withContext(dispatchers.io) {
                    val rows = repo.getSummaryRows(fromIso, toIso)
                    val receipts = repo.getReceiptsInRange(fromIso, toIso)
                    rows to receipts
                }
            }.onSuccess { (rows, receipts) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        rows = rows,
                        receipts = receipts
                    )
                }
            }.onFailure { ex ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Load failed",
                        rows = emptyList(),
                        receipts = emptyList()
                    )
                }
            }
        }
    }

    private fun loadMonth(anchor: LocalDate) {
        val (fromIso, toIso) = computeMonthRange(anchor)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    mode = SummaryMode.MONTH,
                    isLoading = true,
                    errorMessage = null,
                    anchorDateIso = anchor.toString(),
                    selectedMonthNumber = anchor.monthNumber,
                    rows = emptyList(),
                    receipts = emptyList()
                )
            }

            runCatching {
                withContext(dispatchers.io) {
                    val rows = repo.getSummaryRows(fromIso, toIso)
                    val receipts = repo.getReceiptsInRange(fromIso, toIso)
                    rows to receipts
                }
            }.onSuccess { (rows, receipts) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        rows = rows,
                        receipts = receipts
                    )
                }
            }.onFailure { ex ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Load failed",
                        rows = emptyList(),
                        receipts = emptyList()
                    )
                }
            }
        }
    }

    private fun changeMonth(monthNumber: Int) {
        val cur = parseIsoOrToday(_state.value.anchorDateIso)
        val m = monthNumber.coerceIn(1, 12)
        loadMonth(LocalDate(cur.year, m, 1))
    }

    private fun exportMonthPdf() {
        val s = _state.value
        if (s.mode != SummaryMode.MONTH) {
            _state.update { it.copy(exportResultMessage = "Chỉ export theo Tháng.", exportPath = null) }
            return
        }

        val anchor = parseIsoOrToday(s.anchorDateIso)
        val (fromIso, toIso) = computeMonthRange(anchor)
        val monthLabel = "Tháng ${anchor.monthNumber}"

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResultMessage = null, exportPath = null) }

            runCatching {
                withContext(dispatchers.io) {
                    // ✅ dùng data hiện tại nếu đã load sẵn; nếu trống thì query lại
                    val rows = if (s.rows.isNotEmpty()) s.rows else repo.getSummaryRows(fromIso, toIso)
                    val receipts = if (s.receipts.isNotEmpty()) s.receipts else repo.getReceiptsInRange(fromIso, toIso)

                    pdfExporter.exportMonthPdf(
                        monthLabel = monthLabel,
                        fromDateIso = fromIso,
                        toDateIso = toIso,
                        rows = rows,
                        receipts = receipts
                    )
                }
            }.onSuccess { path ->
                _state.update {
                    it.copy(
                        isExporting = false,
                        exportResultMessage = "Đã xuất PDF thành công.",
                        exportPath = path
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isExporting = false,
                        exportResultMessage = "Đã có lỗi xảy ra. Vui lòng thử lại.",
                        exportPath = null
                    )
                }
            }
        }
    }

    private fun computeMonthRange(anchor: LocalDate): Pair<String, String> {
        val first = LocalDate(anchor.year, anchor.monthNumber, 1)
        val firstNext = first + DatePeriod(months = 1)
        val last = firstNext - DatePeriod(days = 1)
        return first.toString() to last.toString()
    }

    private fun parseIsoOrToday(iso: String): LocalDate =
        runCatching { LocalDate.parse(iso) }.getOrElse { Clock.System.todayIn(tz) }
}