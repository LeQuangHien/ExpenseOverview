package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.repository.AuditRepository
import com.hien.le.expenseoverview.platform.Clock

class PurgeOldAudit(
    private val repo: AuditRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(days: Int = 365) {
        val cutoff = clock.nowEpochMillis() - days.toLong() * 24L * 60L * 60L * 1000L
        repo.purgeOlderThan(cutoff)
    }
}