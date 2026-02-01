package com.hien.le.expenseoverview.presentation.entry

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

    // expense list
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
    }
}

sealed interface EntryAction {
    data class Load(val dateIso: String) : EntryAction
    data class ChangeDate(val dateIso: String) : EntryAction

    data class EditBargeld(val text: String) : EntryAction
    data class EditKarte(val text: String) : EntryAction
    data class EditNote(val text: String) : EntryAction

    // Expense add
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