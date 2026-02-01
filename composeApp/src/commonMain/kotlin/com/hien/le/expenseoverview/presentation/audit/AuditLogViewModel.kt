package com.hien.le.expenseoverview.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.usecase.GetAuditEvents
import com.hien.le.expenseoverview.domain.usecase.PurgeOldAudit
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuditLogViewModel(
    private val getAuditEvents: GetAuditEvents,
    private val purgeOldAudit: PurgeOldAudit,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(AuditState())
    val state: StateFlow<AuditState> = _state.asStateFlow()

    private val _effects = Channel<AuditEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<AuditEffect> = _effects.receiveAsFlow()

    fun dispatch(action: AuditAction) {
        when (action) {
            is AuditAction.LoadByDate -> loadByDate(action.dateIso)
            is AuditAction.LoadByRange -> loadByRange(action.fromEpochMs, action.toEpochMs)
            AuditAction.PurgeOld -> purge()
            AuditAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadByDate(dateIso: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, dateIso = dateIso, fromEpochMs = null, toEpochMs = null) }

            runCatching {
                withContext(dispatchers.io) { getAuditEvents.byDate(dateIso) }
            }.onSuccess { list ->
                _state.update { it.copy(isLoading = false, events = list) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải log") }
            }
        }
    }

    private fun loadByRange(from: Long, to: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, dateIso = null, fromEpochMs = from, toEpochMs = to) }

            runCatching {
                withContext(dispatchers.io) { getAuditEvents.inRange(from, to) }
            }.onSuccess { list ->
                _state.update { it.copy(isLoading = false, events = list) }
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi tải log") }
            }
        }
    }

    private fun purge() {
        viewModelScope.launch {
            runCatching { withContext(dispatchers.io) { purgeOldAudit() } }
                .onSuccess { _effects.trySend(AuditEffect.Toast("Đã dọn log cũ (> 1 năm)")) }
                .onFailure { ex -> _state.update { it.copy(errorMessage = ex.message ?: "Lỗi purge log") } }
        }
    }
}