package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.repository.AuditRepository

class GetAuditEvents(private val repo: AuditRepository) {
    suspend fun byDate(dateIso: String): List<AuditEvent> = repo.listByDate(dateIso)
    suspend fun inRange(fromEpochMs: Long, toEpochMs: Long): List<AuditEvent> = repo.listInRange(fromEpochMs, toEpochMs)
}