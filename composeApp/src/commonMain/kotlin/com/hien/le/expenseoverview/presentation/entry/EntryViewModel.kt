package com.hien.le.expenseoverview.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.usecase.AddExpenseItem
import com.hien.le.expenseoverview.domain.usecase.DeleteExpenseItem
import com.hien.le.expenseoverview.domain.usecase.GetDailyEntry
import com.hien.le.expenseoverview.domain.usecase.GetExpenseItemsByDate
import com.hien.le.expenseoverview.domain.usecase.UpsertDailyEntryWithAudit
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import com.hien.le.expenseoverview.presentation.common.MoneyInput
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class EntryViewModel(
    private val getDailyEntry: GetDailyEntry,
    private val upsertWithAudit: UpsertDailyEntryWithAudit,
    private val getExpenseItemsByDate: GetExpenseItemsByDate,
    private val addExpenseItem: AddExpenseItem,
    private val deleteExpenseItem: DeleteExpenseItem,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryState.initial(todayIso()))
    val state: StateFlow<EntryState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<EntryEffect>(extraBufferCapacity = 16)
    val effects: Flow<EntryEffect> = _effects.asSharedFlow()

    fun dispatch(action: EntryAction) {
        when (action) {
            is EntryAction.Load -> load(action.dateIso)
            is EntryAction.ChangeDate -> load(action.dateIso)

            is EntryAction.EditBargeld -> updateMoney(Field.BARGELD, action.text)
            is EntryAction.EditKarte -> updateMoney(Field.KARTE, action.text)
            is EntryAction.EditNote -> _state.update { it.copy(noteText = action.text) }

            is EntryAction.SelectVendor ->
                _state.update { it.copy(vendorPreset = action.preset).recalcAddExpenseEnabled() }

            is EntryAction.EditVendorCustom ->
                _state.update { it.copy(vendorCustomText = action.text).recalcAddExpenseEnabled() }

            is EntryAction.EditExpenseAmount ->
                _state.update { it.copy(expenseAmountText = action.text).recalcAddExpenseEnabled() }

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
                EntryState.initial(dateIso)
            } else {
                EntryState.initial(dateIso).copy(
                    bargeldText = formatCents(entry.bargeld.value),
                    karteText = formatCents(entry.karte.value),
                    noteText = entry.note.orEmpty(),
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

            _state.value = base
                .copy(expenseItems = uiItems, isLoading = false)
                .recalcTotals()
                .recalcAddExpenseEnabled()
        }
    }

    private fun updateMoney(kind: Field, text: String) {
        _state.update { s ->
            val next = when (kind) {
                Field.BARGELD -> s.copy(bargeldText = text)
                Field.KARTE -> s.copy(karteText = text)
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
            _state.update { it.copy(errorMessage = "VENDOR_EMPTY") }
            return
        }
        if (cents == null || cents < 0) {
            _state.update { it.copy(errorMessage = "AMOUNT_INVALID") }
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
            }.onFailure {
                _state.update { it.copy(errorMessage = "ADD_EXPENSE_FAILED") }
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
            }.onFailure {
                _state.update { it.copy(errorMessage = "DELETE_EXPENSE_FAILED") }
            }
        }
    }

    private fun save(comment: String?) {
        val s = _state.value
        val bargeld = MoneyInput.parseToCents(s.bargeldText)
        val karte = MoneyInput.parseToCents(s.karteText)

        if (bargeld == null || karte == null) {
            _state.update { it.copy(errorMessage = "SAVE_INVALID_INPUT") }
            return
        }
        if (s.bargeldText.trim().isEmpty() || s.karteText.trim().isEmpty()) {
            _state.update { it.copy(errorMessage = "SAVE_EMPTY_REQUIRED") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                withContext(dispatchers.io) {
                    upsertWithAudit(
                        UpsertDailyEntryWithAudit.Input(
                            dateIso = s.dateIso,
                            bargeld = Cents(bargeld),
                            karte = Cents(karte),
                            expense = Cents(0), // legacy param; bạn có thể bỏ khỏi usecase sau
                            note = s.noteText.ifBlank { null },
                            comment = comment
                        )
                    )
                }
            }.onSuccess {
                // ✅ Clear everything & reset to today
                _state.value = EntryState.initial(todayIso())
                _effects.tryEmit(EntryEffect.SaveSuccess)
            }.onFailure {
                _state.update { it.copy(isLoading = false, errorMessage = "SAVE_FAILED") }
            }
        }
    }

    // ---------- helpers ----------

    private fun EntryState.recalcTotals(): EntryState {
        val b = MoneyInput.parseToCents(bargeldText)
        val k = MoneyInput.parseToCents(karteText)

        val b0 = b ?: 0L
        val k0 = k ?: 0L

        val revenue = b0 + k0
        val expenseTotal = expenseItems.sumOf { it.amountCents }
        val net = revenue - expenseTotal

        val requiredFilled = bargeldText.trim().isNotEmpty() && karteText.trim().isNotEmpty()
        val valid = (b != null && k != null && requiredFilled)

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

    private fun todayIso(): String =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    private fun formatCents(cents: Long): String {
        val euros = cents / 100
        val c = (cents % 100).toString().padStart(2, '0')
        return "$euros,$c"
    }

    private enum class Field { BARGELD, KARTE }
}