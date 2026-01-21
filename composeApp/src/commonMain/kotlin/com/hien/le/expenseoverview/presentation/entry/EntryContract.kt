package com.hien.le.expenseoverview.presentation.entry

data class EntryState(
    val dateIso: String,
    val bargeldText: String = "",
    val karteText: String = "",
    val expenseText: String = "",
    val noteText: String = "",
    val totalRevenueCents: Long = 0,
    val netCents: Long = 0,
    val isLoading: Boolean = false,
    val canSave: Boolean = true,
    val errorMessage: String? = null,
    val lastSavedAt: Long? = null
)

sealed interface EntryAction {
    data class Load(val dateIso: String) : EntryAction
    data class ChangeDate(val dateIso: String) : EntryAction
    data class EditBargeld(val text: String) : EntryAction
    data class EditKarte(val text: String) : EntryAction
    data class EditExpense(val text: String) : EntryAction
    data class EditNote(val text: String) : EntryAction
    data class Save(val auditComment: String? = null) : EntryAction
    data object ClearError : EntryAction
}

sealed interface EntryEffect {
    data class Toast(val message: String) : EntryEffect
}