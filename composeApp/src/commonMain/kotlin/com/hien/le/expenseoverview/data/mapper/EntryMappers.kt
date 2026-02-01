package com.hien.le.expenseoverview.data.mapper

import com.hien.le.expenseoverview.db.DailyEntryEntity
import com.hien.le.expenseoverview.domain.model.Cents
import com.hien.le.expenseoverview.domain.model.DailyEntry

fun DailyEntryEntity.toDomain(): DailyEntry =
    DailyEntry(
        dateIso = dateIso,
        bargeld = Cents(bargeldCents),
        karte = Cents(karteCents),
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun DailyEntry.toEntity(): DailyEntryEntity =
    DailyEntryEntity(
        dateIso = dateIso,
        bargeldCents = bargeld.value,
        karteCents = karte.value,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )