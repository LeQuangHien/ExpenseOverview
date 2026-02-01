package com.hien.le.expenseoverview

import com.hien.le.expenseoverview.domain.usecase.*
import com.hien.le.expenseoverview.platform.Clock
import com.hien.le.expenseoverview.presentation.common.CoroutineDispatchers

interface AppContainer {
    val getDailyEntry: GetDailyEntry
    val upsertDailyEntryWithAudit: UpsertDailyEntryWithAudit
    val getSummary: GetSummary
    val getAuditEvents: GetAuditEvents
    val purgeOldAudit: PurgeOldAudit

    val clock: Clock
    val dispatchers: CoroutineDispatchers
}