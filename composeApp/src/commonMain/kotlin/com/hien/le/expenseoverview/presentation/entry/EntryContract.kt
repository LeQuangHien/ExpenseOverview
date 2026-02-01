package com.hien.le.expenseoverview.presentation.entry

data class ExpenseItemUi(
    val id: String,
    val vendorName: String,
    val amountText: String,   // raw input like "12,30"
    val amountCents: Long
)

data class EntryState(
    val dateIso: String,
    val bargeldText: String = "",
    val karteText: String = "",
    val noteText: String = "",

    // Expense add form
    val vendorPreset: VendorPreset = VendorPreset.ALDI_SUED,
    val vendorCustomText: String = "",
    val expenseAmountText: String = "",

    // Items
    val expenseItems: List<ExpenseItemUi> = emptyList(),

    // Totals
    val totalRevenueCents: Long = 0,
    val totalExpenseCents: Long = 0,
    val netCents: Long = 0,

    val isLoading: Boolean = false,
    val canSave: Boolean = true,
    val canAddExpense: Boolean = false,
    val errorMessage: String? = null,
    val lastSavedAt: Long? = null
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

sealed interface EntryEffect {
    data class Toast(val message: String) : EntryEffect
}