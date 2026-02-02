package com.hien.le.expenseoverview.domain.model

data class AuditEvent(
    val id: String,
    val entityDateIso: String,

    /** Field name: BARGELD / KARTE / EXPENSE_TOTAL */
    val field: String,

    val oldValue: String,
    val newValue: String,

    /** epoch millis */
    val editedAt: Long,

    /** optional user comment */
    val comment: String?
)