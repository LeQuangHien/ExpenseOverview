package com.hien.le.expenseoverview.domain.usecase

import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.model.DailyEntry
import com.hien.le.expenseoverview.domain.repository.AuditRepository
import com.hien.le.expenseoverview.domain.repository.EntryRepository
import com.hien.le.expenseoverview.platform.Clock
import kotlin.random.Random

class UpsertDailyEntryWithAudit(
    private val entryRepo: EntryRepository,
    private val auditRepo: AuditRepository,
    private val clock: Clock
) {
    data class Input(
        val dateIso: String,
        val bargeld: Cents,
        val karte: Cents,
        val expense: Cents,
        val note: String?,
        val comment: String?
    )

    suspend operator fun invoke(input: Input) {
        val now = clock.nowEpochMillis()
        val existing = entryRepo.getByDate(input.dateIso)

        val createdAt = existing?.createdAt ?: now
        val newEntry = DailyEntry(
            dateIso = input.dateIso,
            bargeld = input.bargeld,
            karte = input.karte,
            note = input.note,
            createdAt = createdAt,
            updatedAt = now
        )

        if (existing != null) {
            diffAndWriteAudit(existing, newEntry, input.comment, now)
        }

        entryRepo.upsert(newEntry)
    }

    private suspend fun diffAndWriteAudit(old: DailyEntry, new: DailyEntry, comment: String?, now: Long) {
        fun id(): String = "ae_${now}_${Random.nextInt(100000, 999999)}"

        suspend fun emit(field: String, oldV: String, newV: String) {
            if (oldV == newV) return
            auditRepo.insert(
                AuditEvent(
                    id = id(),
                    entityDateIso = old.dateIso,
                    field = field,
                    oldValue = oldV,
                    newValue = newV,
                    editedAt = now,
                    comment = comment
                )
            )
        }

        emit("bargeld", old.bargeld.value.toString(), new.bargeld.value.toString())
        emit("karte", old.karte.value.toString(), new.karte.value.toString())
        emit("note", old.note.orEmpty(), new.note.orEmpty())
    }
}