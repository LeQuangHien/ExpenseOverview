package com.hien.le.expenseoverview.data.repository

import com.hien.le.expenseoverview.data.mapper.toDomain
import com.hien.le.expenseoverview.data.mapper.toEntity
import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.repository.AuditRepository

class AuditRepositoryImpl(
    private val dao: ExpenseDao
) : AuditRepository {

    override suspend fun insert(event: AuditEvent) {
        dao.insertAuditEvent(event.toEntity())
    }

    override suspend fun listByDate(dateIso: String): List<AuditEvent> =
        dao.listAuditByDate(dateIso).map { it.toDomain() }

    override suspend fun listInRange(fromEpochMs: Long, toEpochMs: Long): List<AuditEvent> =
        dao.listAuditInRange(fromEpochMs, toEpochMs).map { it.toDomain() }

    override suspend fun purgeOlderThan(cutoffEpochMs: Long) {
        dao.purgeAuditOlderThan(cutoffEpochMs)
    }
}