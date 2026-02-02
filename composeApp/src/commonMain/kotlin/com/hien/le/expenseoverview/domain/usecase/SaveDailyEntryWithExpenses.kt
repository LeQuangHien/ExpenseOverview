package com.hien.le.expenseoverview.domain.usecase

import androidx.room.Transaction
import com.hien.le.expenseoverview.db.DailyEntryEntity
import com.hien.le.expenseoverview.db.ExpenseDao
import com.hien.le.expenseoverview.db.ExpenseItemEntity
import com.hien.le.expenseoverview.domain.model.AuditEvent
import com.hien.le.expenseoverview.domain.model.AuditFields
import com.hien.le.expenseoverview.domain.repository.AuditRepository
import com.hien.le.expenseoverview.presentation.common.Utils.generateUuid
import kotlin.time.Clock

class SaveDailyEntryWithExpensesUseCase(
    private val dao: ExpenseDao,
    private val auditRepository: AuditRepository,
) {
    data class ExpenseDraft(
        val vendorName: String,
        val amountCents: Long
    )

    data class Input(
        val dateIso: String,
        val bargeldCents: Long,
        val karteCents: Long,
        val note: String?,
        val expenses: List<ExpenseDraft>,
        val auditComment: String? = null
    )

    @Transaction
    suspend fun execute(input: Input) {
        val dateIso = input.dateIso
        val now = Clock.System.now().toEpochMilliseconds()

        // --- OLD values (from DB) ---
        val oldEntry = dao.getEntryByDate(dateIso)
        val oldBargeld = oldEntry?.bargeldCents ?: 0L
        val oldKarte = oldEntry?.karteCents ?: 0L
        val oldExpenseTotal = dao.sumExpenseCentsByDate(dateIso)

        // --- NEW values (from UI draft) ---
        val newBargeld = input.bargeldCents
        val newKarte = input.karteCents
        val newExpenseTotal = input.expenses.sumOf { it.amountCents }

        // --- AUDIT: log only changed fields ---
        if (oldBargeld != newBargeld) {
            auditRepository.insert(
                AuditEvent(
                    id = generateUuid(),
                    entityDateIso = dateIso,
                    field = AuditFields.BARGELD,
                    oldValue = oldBargeld.toString(),
                    newValue = newBargeld.toString(),
                    editedAt = now,
                    comment = input.auditComment
                )
            )
        }

        if (oldKarte != newKarte) {
            auditRepository.insert(
                AuditEvent(
                    id = generateUuid(),
                    entityDateIso = dateIso,
                    field = AuditFields.KARTE,
                    oldValue = oldKarte.toString(),
                    newValue = newKarte.toString(),
                    editedAt = now,
                    comment = input.auditComment
                )
            )
        }

// ✅ Expense: log tổng chi tiêu cũ -> mới
        if (oldExpenseTotal != newExpenseTotal) {
            auditRepository.insert(
                AuditEvent(
                    id = generateUuid(),
                    entityDateIso = dateIso,
                    field = AuditFields.EXPENSE_TOTAL,
                    oldValue = oldExpenseTotal.toString(),
                    newValue = newExpenseTotal.toString(),
                    editedAt = now,
                    comment = input.auditComment
                )
            )
        }

        // --- Upsert daily entry ---
        val createdAt = oldEntry?.createdAt ?: now

        dao.upsertEntry(
            DailyEntryEntity(
                dateIso = dateIso,
                bargeldCents = newBargeld,
                karteCents = newKarte,
                note = input.note,
                createdAt = createdAt,
                updatedAt = now
            )
        )

        // --- Replace ALL expense items for that date ---
        dao.deleteExpenseItemsByDate(dateIso)
        input.expenses.forEach { e ->
            dao.upsertExpenseItem(
                ExpenseItemEntity(
                    id = generateUuid(),
                    dateIso = dateIso,
                    vendorName = e.vendorName,
                    amountCents = e.amountCents,
                    createdAt = now
                )
            )
        }
    }
}