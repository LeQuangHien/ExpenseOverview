package com.hien.le.expenseoverview.presentation.audit

data class AuditEventUi(
    val entityDateIso: String,
    val field: String,
    val oldValue: String,
    val newValue: String,
    val editedAt: Long,
    val comment: String?
)

data class AuditState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val events: List<AuditEventUi> = emptyList()
)

sealed interface AuditAction {
    data class LoadByDate(val dateIso: String) : AuditAction
    data class LoadByRange(val fromEpochMs: Long, val toEpochMs: Long) : AuditAction
    data object PurgeOld : AuditAction
    data object ClearError : AuditAction
}

sealed interface AuditEffect {
    data class Toast(val message: String) : AuditEffect
}