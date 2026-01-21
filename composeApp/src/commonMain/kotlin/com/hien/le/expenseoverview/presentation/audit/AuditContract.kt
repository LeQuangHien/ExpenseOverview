package com.hien.le.expenseoverview.presentation.audit

import com.hien.le.expenseoverview.domain.model.AuditEvent

data class AuditState(
    val dateIso: String? = null,
    val fromEpochMs: Long? = null,
    val toEpochMs: Long? = null,
    val isLoading: Boolean = false,
    val events: List<AuditEvent> = emptyList(),
    val errorMessage: String? = null
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