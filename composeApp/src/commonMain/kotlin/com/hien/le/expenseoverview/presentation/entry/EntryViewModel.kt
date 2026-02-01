package com.hien.le.expenseoverview.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.usecase.*
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
    private val getExpenseItemsByDate: GetExpenseItemsByDate,
    private val addExpenseItem: AddExpenseItem,
    private val deleteExpenseItem: DeleteExpenseItem,
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

            is EntryAction.EditBargeld -> updateMoney(text = action.text, kind = Field.BARGELD)
            is EntryAction.EditKarte -> updateMoney(text = action.text, kind = Field.KARTE)
            is EntryAction.EditNote -> _state.update { it.copy(noteText = action.text) }

            is EntryAction.SelectVendor -> _state.update { it.copy(vendorPreset = action.preset).recalcAddExpenseEnabled() }
            is EntryAction.EditVendorCustom -> _state.update { it.copy(vendorCustomText = action.text).recalcAddExpenseEnabled() }
            is EntryAction.EditExpenseAmount -> _state.update { it.copy(expenseAmountText = action.text).recalcAddExpenseEnabled() }

            EntryAction.AddExpenseItem -> addExpense()
            is EntryAction.DeleteExpenseItem -> deleteExpense(action.id)

            is EntryAction.Save -> save(action.auditComment)
            EntryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun load(dateIso: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, dateIso = dateIso) }

            val entry = withContext(dispatchers.io) { getDailyEntry(dateIso) }
            val receipts = withContext(dispatchers.io) { getExpenseItemsByDate(dateIso) }

            val base = if (entry == null) {
                EntryState(dateIso = dateIso)
            } else {
                EntryState(
                    dateIso = dateIso,
                    bargeldText = formatCents(entry.bargeld.value),
                    karteText = formatCents(entry.karte.value),
                    noteText = entry.note.orEmpty()
                )
            }

            val uiItems = receipts.map {
                ExpenseItemUi(
                    id = it.id,
                    vendorName = it.vendorName,
                    amountText = formatCents(it.amount.value),
                    amountCents = it.amount.value
                )
            }

            _state.value = base.copy(expenseItems = uiItems).recalcTotals().recalcAddExpenseEnabled().copy(isLoading = false)
        }
    }

    private fun updateMoney(text: String, kind: Field) {
        _state.update {
            val next = when (kind) {
                Field.BARGELD -> it.copy(bargeldText = text)
                Field.KARTE -> it.copy(karteText = text)
            }
            next.recalcTotals()
        }
    }

    private fun addExpense() {
        val s = _state.value

        val vendor = when (s.vendorPreset) {
            VendorPreset.OTHER -> s.vendorCustomText.trim()
            else -> s.vendorPreset.label
        }
        val cents = MoneyInput.parseToCents(s.expenseAmountText)

        if (vendor.isBlank()) {
            _state.update { it.copy(errorMessage = "Vui lòng nhập tên nơi mua") }
            return
        }
        if (cents == null) {
            _state.update { it.copy(errorMessage = "Số tiền chi tiêu không hợp lệ") }
            return
        }

        viewModelScope.launch {
            runCatching {
                withContext(dispatchers.io) {
                    addExpenseItem(AddExpenseItem.Input(s.dateIso, vendor, Cents(cents)))
                }
            }.onSuccess { item ->
                _state.update { cur ->
                    val nextItems = cur.expenseItems + ExpenseItemUi(
                        id = item.id,
                        vendorName = item.vendorName,
                        amountText = formatCents(item.amount.value),
                        amountCents = item.amount.value
                    )
                    cur.copy(
                        expenseItems = nextItems,
                        expenseAmountText = "",
                        vendorCustomText = if (cur.vendorPreset == VendorPreset.OTHER) "" else cur.vendorCustomText
                    ).recalcTotals().recalcAddExpenseEnabled()
                }
            }.onFailure { ex ->
                _state.update { it.copy(errorMessage = ex.message ?: "Lỗi thêm chi tiêu") }
            }
        }
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatchers.io) { deleteExpenseItem(id) }
            }.onSuccess {
                _state.update { cur ->
                    cur.copy(expenseItems = cur.expenseItems.filterNot { it.id == id })
                        .recalcTotals()
                        .recalcAddExpenseEnabled()
                }
            }.onFailure { ex ->
                _state.update { it.copy(errorMessage = ex.message ?: "Lỗi xóa chi tiêu") }
            }
        }
    }

    private fun save(comment: String?) {
        val s = _state.value
        val b = MoneyInput.parseToCents(s.bargeldText)
        val k = MoneyInput.parseToCents(s.karteText)

        if (b == null || k == null) {
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
                            expense = Cents(0), // legacy param; you can remove from usecase later
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

    private fun EntryState.recalcTotals(): EntryState {
        val b = MoneyInput.parseToCents(bargeldText) ?: 0L
        val k = MoneyInput.parseToCents(karteText) ?: 0L
        val revenue = b + k
        val expenseTotal = expenseItems.sumOf { it.amountCents }
        val net = revenue - expenseTotal

        val valid = MoneyInput.parseToCents(bargeldText) != null &&
                MoneyInput.parseToCents(karteText) != null

        return copy(
            totalRevenueCents = revenue,
            totalExpenseCents = expenseTotal,
            netCents = net,
            canSave = valid
        )
    }

    private fun EntryState.recalcAddExpenseEnabled(): EntryState {
        val vendorOk = when (vendorPreset) {
            VendorPreset.OTHER -> vendorCustomText.trim().isNotBlank()
            else -> true
        }
        val amountOk = MoneyInput.parseToCents(expenseAmountText) != null
        return copy(canAddExpense = vendorOk && amountOk)
    }

    private fun formatCents(cents: Long): String {
        val euros = cents / 100
        val c = (cents % 100).toString().padStart(2, '0')
        return "$euros,$c"
    }

    private enum class Field { BARGELD, KARTE }
}