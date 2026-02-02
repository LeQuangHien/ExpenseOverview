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
                val from = now - 30L * 24L * 60L * 60L * 1000L // 30 ngày gần nhất
                withContext(dispatchers.io) {
                    repo.listInRange(from, now)
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