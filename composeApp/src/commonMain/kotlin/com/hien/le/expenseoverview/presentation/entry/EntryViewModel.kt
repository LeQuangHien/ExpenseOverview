package com.hien.le.expenseoverview.presentation.entry

import GetEntryWithExpensesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hien.le.expenseoverview.domain.usecase.SaveDailyEntryWithExpensesUseCase
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers
import com.hien.le.expenseoverview.presentation.common.MoneyInput
import com.hien.le.expenseoverview.presentation.common.Utils.generateUuid
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EntryViewModel(
    private val getEntryWithExpenses: GetEntryWithExpensesUseCase,
    private val saveDailyEntryWithExpenses: SaveDailyEntryWithExpensesUseCase,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryState.initial(todayIso()))
    val state: StateFlow<EntryState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<EntryEffect>(extraBufferCapacity = 16)
    val effects: Flow<EntryEffect> = _effects.asSharedFlow()

    init {
        dispatch(EntryAction.Load(todayIso()))
    }

    fun dispatch(action: EntryAction) {
        when (action) {
            is EntryAction.ChangeDate -> dispatch(EntryAction.Load(action.dateIso))
            is EntryAction.Load -> loadDate(action.dateIso)

            is EntryAction.EditBargeld -> _state.update { it.copy(bargeldText = action.text).recalcTotals() }
            is EntryAction.EditKarte -> _state.update { it.copy(karteText = action.text).recalcTotals() }
            is EntryAction.EditNote -> _state.update { it.copy(noteText = action.text) }

            is EntryAction.SelectVendor ->
                _state.update { it.copy(vendorPreset = action.preset).recalcAddExpenseEnabled() }

            is EntryAction.EditVendorCustom ->
                _state.update { it.copy(vendorCustomText = action.text).recalcAddExpenseEnabled() }

            is EntryAction.EditExpenseAmount ->
                _state.update { it.copy(expenseAmountText = action.text).recalcAddExpenseEnabled() }

            EntryAction.AddExpenseItem -> addExpenseDraft()
            is EntryAction.DeleteExpenseItem -> deleteExpenseDraft(action.id)

            is EntryAction.Save -> save(action.auditComment)
            EntryAction.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadDate(dateIso: String) {
        viewModelScope.launch {
            _state.value = EntryState.initial(dateIso).copy(isLoading = true, errorMessage = null)

            runCatching {
                withContext(dispatchers.io) { getEntryWithExpenses.execute(dateIso) }
            }.onSuccess { loaded ->
                if (loaded == null) {
                    // No data in DB -> blank draft for that date
                    _state.value = EntryState.initial(dateIso)
                } else {
                    _state.value = EntryState.fromLoaded(
                        dateIso = loaded.dateIso,
                        bargeldCents = loaded.bargeldCents,
                        karteCents = loaded.karteCents,
                        note = loaded.note,
                        expenses = loaded.expenses.map {
                            LoadedExpenseItem(
                                id = it.id,
                                vendorName = it.vendorName,
                                amountCents = it.amountCents
                            )
                        }
                    )
                }
            }.onFailure {
                // If load fails -> still allow editing blank draft
                _state.value = EntryState.initial(dateIso)
            }
        }
    }

    private fun addExpenseDraft() {
        val s = _state.value
        val vendor = when (s.vendorPreset) {
            VendorPreset.OTHER -> s.vendorCustomText.trim()
            else -> s.vendorPreset.label
        }
        val cents = MoneyInput.parseToCents(s.expenseAmountText)

        if (vendor.isBlank() || cents == null || cents < 0) {
            _state.update { it.copy(errorMessage = "INVALID_EXPENSE") }
            return
        }

        val item = ExpenseItemUi(
            id = generateUuid(),
            vendorName = vendor,
            amountText = s.expenseAmountText,
            amountCents = cents
        )

        _state.update {
            it.copy(
                expenseItems = it.expenseItems + item,
                expenseAmountText = "",
                vendorCustomText = if (it.vendorPreset == VendorPreset.OTHER) "" else it.vendorCustomText
            ).recalcTotals().recalcAddExpenseEnabled()
        }
    }

    private fun deleteExpenseDraft(id: String) {
        _state.update {
            it.copy(expenseItems = it.expenseItems.filterNot { e -> e.id == id })
                .recalcTotals()
                .recalcAddExpenseEnabled()
        }
    }

    private fun save(comment: String?) {
        val s = _state.value

        val bargeld = MoneyInput.parseToCents(s.bargeldText)
        val karte = MoneyInput.parseToCents(s.karteText)

        if (s.bargeldText.trim().isEmpty() || s.karteText.trim().isEmpty()) {
            _state.update { it.copy(errorMessage = "REQUIRED_EMPTY") }
            return
        }
        if (bargeld == null || karte == null) {
            _state.update { it.copy(errorMessage = "INVALID_MONEY") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                withContext(dispatchers.io) {
                    saveDailyEntryWithExpenses.execute(
                        SaveDailyEntryWithExpensesUseCase.Input(
                            dateIso = s.dateIso,
                            bargeldCents = bargeld,
                            karteCents = karte,
                            note = s.noteText.ifBlank { null },
                            expenses = s.expenseItems.map {
                                SaveDailyEntryWithExpensesUseCase.ExpenseDraft(
                                    vendorName = it.vendorName,
                                    amountCents = it.amountCents
                                )
                            },
                            auditComment = comment
                        )
                    )
                }
            }.onSuccess {
                // ✅ theo yêu cầu: save xong clear màn hình về hôm nay
                _state.value = EntryState.initial(todayIso())
                _effects.tryEmit(EntryEffect.SaveSuccess)
            }.onFailure {
                _state.update { it.copy(isLoading = false, errorMessage = "SAVE_FAILED") }
            }
        }
    }

    private fun todayIso(): String {
        val ms = Clock.System.now().toEpochMilliseconds()
        val tz = TimeZone.currentSystemDefault()
        return Instant.fromEpochMilliseconds(ms).toLocalDateTime(tz).date.toString()
    }
}