package com.hien.le.expenseoverview.domain.repository

import com.hien.le.expenseoverview.domain.model.AuditEvent

interface AuditRepository {
    suspend fun insert(event: AuditEvent)
    suspend fun listByDate(dateIso: String): List<AuditEvent>
    suspend fun listInRange(fromEpochMs: Long, toEpochMs: Long): List<AuditEvent>
    suspend fun purgeOlderThan(cutoffEpochMs: Long)
}