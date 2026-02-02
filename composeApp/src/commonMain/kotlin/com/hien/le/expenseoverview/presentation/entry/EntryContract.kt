package com.hien.le.expenseoverview.presentation.entry

import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.common.MoneyInput

data class ExpenseItemUi(
    val id: String,
    val vendorName: String,
    val amountText: String,   // raw input like "12,30"
    val amountCents: Long
)

enum class VendorPreset(val label: String) {
    ALDI_SUED("Aldi Süd"),
    REWE("Rewe"),
    BAUHAUS("Bauhaus"),
    LIDL("Lidl"),
    EDEKA("Edeka"),
    DM("dm"),
    OTHER("Khác")
}

data class EntryState(
    val dateIso: String,

    // revenue
    val bargeldText: String = "",
    val karteText: String = "",

    // note
    val noteText: String = "",

    // expense add form
    val vendorPreset: VendorPreset = VendorPreset.ALDI_SUED,
    val vendorCustomText: String = "",
    val expenseAmountText: String = "",

    // expense list (draft)
    val expenseItems: List<ExpenseItemUi> = emptyList(),

    // totals
    val totalRevenueCents: Long = 0,
    val totalExpenseCents: Long = 0,
    val netCents: Long = 0,

    // ui flags
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val canAddExpense: Boolean = false,

    // errors
    val errorMessage: String? = null
) {
    companion object {
        fun initial(dateIso: String): EntryState =
            EntryState(
                dateIso = dateIso,
                bargeldText = "",
                karteText = "",
                noteText = "",
                vendorPreset = VendorPreset.ALDI_SUED,
                vendorCustomText = "",
                expenseAmountText = "",
                expenseItems = emptyList(),
                totalRevenueCents = 0,
                totalExpenseCents = 0,
                netCents = 0,
                isLoading = false,
                canSave = false,
                canAddExpense = false,
                errorMessage = null
            )

        /**
         * Map data loaded from DB -> UI editable state
         *
         * expenses: list from DB (id/vendorName/amountCents)
         */
        fun fromLoaded(
            dateIso: String,
            bargeldCents: Long,
            karteCents: Long,
            note: String?,
            expenses: List<LoadedExpenseItem>
        ): EntryState {
            val expenseUi = expenses.map { e ->
                ExpenseItemUi(
                    id = e.id,
                    vendorName = e.vendorName,
                    amountText = centsToInputText(e.amountCents),
                    amountCents = e.amountCents
                )
            }

            val s = initial(dateIso).copy(
                bargeldText = centsToInputText(bargeldCents),
                karteText = centsToInputText(karteCents),
                noteText = note.orEmpty(),
                expenseItems = expenseUi
            )
            return s.recalcTotals().recalcAddExpenseEnabled()
        }

        // Input text should be like "12,30" (no € sign)
        private fun centsToInputText(cents: Long): String {
            val euro = MoneyFormatter.centsToDeEuro(cents) // e.g. "12,30 €"
            return euro.replace("€", "").trim()
        }
    }
}

/**
 * Minimal loaded model (to avoid leaking Room entities to commonMain UI state).
 * Your GetEntryWithExpensesUseCase should return these or map to these.
 */
data class LoadedExpenseItem(
    val id: String,
    val vendorName: String,
    val amountCents: Long
)

/** State helpers (pure functions) */
fun EntryState.recalcTotals(): EntryState {
    val b = MoneyInput.parseToCents(bargeldText) ?: 0L
    val k = MoneyInput.parseToCents(karteText) ?: 0L
    val revenue = b + k

    val expenseTotal = expenseItems.sumOf { it.amountCents }
    val net = revenue - expenseTotal

    val requiredFilled = bargeldText.trim().isNotEmpty() && karteText.trim().isNotEmpty()
    val validMoney = MoneyInput.parseToCents(bargeldText) != null && MoneyInput.parseToCents(karteText) != null

    return copy(
        totalRevenueCents = revenue,
        totalExpenseCents = expenseTotal,
        netCents = net,
        canSave = requiredFilled && validMoney
    )
}

fun EntryState.recalcAddExpenseEnabled(): EntryState {
    val vendorOk = when (vendorPreset) {
        VendorPreset.OTHER -> vendorCustomText.trim().isNotBlank()
        else -> true
    }
    val amountOk = MoneyInput.parseToCents(expenseAmountText) != null
    return copy(canAddExpense = vendorOk && amountOk)
}

sealed interface EntryAction {
    data class Load(val dateIso: String) : EntryAction
    data class ChangeDate(val dateIso: String) : EntryAction

    data class EditBargeld(val text: String) : EntryAction
    data class EditKarte(val text: String) : EntryAction
    data class EditNote(val text: String) : EntryAction

    data class SelectVendor(val preset: VendorPreset) : EntryAction
    data class EditVendorCustom(val text: String) : EntryAction
    data class EditExpenseAmount(val text: String) : EntryAction

    data object AddExpenseItem : EntryAction
    data class DeleteExpenseItem(val id: String) : EntryAction

    data class Save(val auditComment: String? = null) : EntryAction
    data object ClearError : EntryAction
}

/**
 * UI effects (one-off events)
 * - SaveSuccess: for showing dialog + focusing Bargeld
 */
sealed interface EntryEffect {
    data object SaveSuccess : EntryEffect
}