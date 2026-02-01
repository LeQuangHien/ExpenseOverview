package com.hien.le.expenseoverview.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.usecase.GetDailyEntry
import com.hien.le.expenseoverview.domain.usecase.UpsertDailyEntryWithAudit
import com.hien.le.expenseoverview.platform.Clock
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import com.hien.le.expenseoverview.presentation.common.MoneyInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryViewModel(
    private val getDailyEntry: GetDailyEntry,
    private val upsertWithAudit: UpsertDailyEntryWithAudit,
    private val clock: Clock,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryState(dateIso = "1970-01-01"))
    val state: StateFlow<EntryState> = _state.asStateFlow()

    private val _effects = Channel<EntryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<EntryEffect> = _effects.receiveAsFlow()

    fun dispatch(action: EntryAction) {
        when (action) {
            is EntryAction.Load -> load(action.dateIso)
            is EntryAction.ChangeDate -> load(action.dateIso)
            is EntryAction.EditBargeld -> updateMoney(action.text, Field.BARGELD)
            is EntryAction.EditKarte -> updateMoney(action.text, Field.KARTE)
            is EntryAction.EditExpense -> updateMoney(action.text, Field.EXPENSE)
            is EntryAction.EditNote -> _state.update { it.copy(noteText = action.text) }
            is EntryAction.Save -> save(action.auditComment)
            EntryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(dateIso: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, dateIso = dateIso) }

            val entry = withContext(dispatchers.io) { getDailyEntry(dateIso) }

            val newState = if (entry == null) {
                EntryState(dateIso = dateIso)
            } else {
                EntryState(
                    dateIso = dateIso,
                    bargeldText = formatCents(entry.bargeld.value),
                    karteText = formatCents(entry.karte.value),
                    expenseText = formatCents(entry.expense.value),
                    noteText = entry.note.orEmpty(),
                ).recalc()
            }

            _state.value = newState.copy(isLoading = false)
        }
    }

    private fun updateMoney(text: String, kind: Field) {
        _state.update {
            val next = when (kind) {
                Field.BARGELD -> it.copy(bargeldText = text)
                Field.KARTE -> it.copy(karteText = text)
                Field.EXPENSE -> it.copy(expenseText = text)
            }
            next.recalc()
        }
    }

    private fun EntryState.recalc(): EntryState {
        val b = MoneyInput.parseToCents(bargeldText)
        val k = MoneyInput.parseToCents(karteText)
        val e = MoneyInput.parseToCents(expenseText)

        val valid = (b != null && k != null && e != null)
        val revenue = (b ?: 0L) + (k ?: 0L)
        val net = revenue - (e ?: 0L)

        return copy(
            totalRevenueCents = revenue,
            netCents = net,
            canSave = valid,
            errorMessage = if (!valid) "Sai định dạng tiền (vd: 12,30)" else errorMessage
        )
    }

    private fun save(comment: String?) {
        val s = _state.value
        val b = MoneyInput.parseToCents(s.bargeldText)
        val k = MoneyInput.parseToCents(s.karteText)
        val e = MoneyInput.parseToCents(s.expenseText)

        if (b == null || k == null || e == null) {
            _state.update { it.copy(errorMessage = "Không thể lưu: dữ liệu tiền không hợp lệ") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                withContext(dispatchers.io) {
                    upsertWithAudit(
                        UpsertDailyEntryWithAudit.Input(
                            dateIso = s.dateIso,
                            bargeld = Cents(b),
                            karte = Cents(k),
                            expense = Cents(e),
                            note = s.noteText.ifBlank { null },
                            comment = comment
                        )
                    )
                }
            }.onSuccess {
                _state.update { it.copy(isLoading = false, lastSavedAt = clock.nowEpochMillis()) }
                _effects.trySend(EntryEffect.Toast("Đã lưu"))
            }.onFailure { ex ->
                _state.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Lỗi khi lưu") }
            }
        }
    }

    private fun formatCents(cents: Long): String {
        val euros = cents / 100
        val c = (cents % 100).toString().padStart(2, '0')
        return "$euros,$c"
    }

    private enum class Field { BARGELD, KARTE, EXPENSE }
}