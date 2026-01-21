package com.hien.le.expenseoverview.domain.model

data class AuditEvent(
    val id: String,
    val entityDateIso: String,
    val field: String,
    val oldValue: String,
    val newValue: String,
    val editedAt: Long,
    val comment: String?
)