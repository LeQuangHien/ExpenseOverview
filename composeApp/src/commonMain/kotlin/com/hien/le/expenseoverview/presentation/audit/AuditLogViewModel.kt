package com.hien.le.expenseoverview.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.repository.AuditRepository
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class AuditLogViewModel(
    private val repo: AuditRepository,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(AuditState(isLoading = true))
    val state: StateFlow<AuditState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** 🔄 Public refresh – dùng cho nút Refresh */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val tz = TimeZone.currentSystemDefault()

                val nowDate = Instant
                    .fromEpochMilliseconds(now)
                    .toLocalDateTime(tz)
                    .date

                // ✅ Giữ: năm hiện tại + năm liền trước
                // Ví dụ:
                // 01.01.2027 -> cutoff = 01.01.2026 -> xóa hết 2025
                // 01.01.2028 -> cutoff = 01.01.2027 -> xóa hết 2026
                val keepFromDate = LocalDate(year = nowDate.year - 1,
                    month = 1,
                    day = 1
                )

                val keepFromEpochMs = keepFromDate
                    .atStartOfDayIn(tz)
                    .toEpochMilliseconds()

                withContext(dispatchers.io) {
                    // ✅ Xóa hết log cũ hơn mốc giữ lại
                    repo.purgeOlderThan(keepFromEpochMs)

                    // ✅ Hiển thị log từ đầu năm trước đến hiện tại
                    repo.listInRange(keepFromEpochMs, now)
                }
            }.onSuccess { list ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        events = list.map { e ->
                            AuditEventUi(
                                entityDateIso = e.entityDateIso,
                                field = e.field,
                                oldValue = e.oldValue,
                                newValue = e.newValue,
                                editedAt = e.editedAt,
                                comment = e.comment
                            )
                        }
                    )
                }
            }.onFailure { ex ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Load log failed"
                    )
                }
            }
        }
    }
}