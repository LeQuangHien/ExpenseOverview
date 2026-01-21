package com.hien.le.expenseoverview.data.mapper

import com.hien.le.expenseoverview.db.AuditEventEntity
import com.hien.le.expenseoverview.domain.model.AuditEvent

fun AuditEventEntity.toDomain(): AuditEvent =
    AuditEvent(
        id = id,
        entityDateIso = entityDateIso,
        field = field,
        oldValue = oldValue,
        newValue = newValue,
        editedAt = editedAt,
        comment = comment
    )

fun AuditEvent.toEntity(): AuditEventEntity =
    AuditEventEntity(
        id = id,
        entityDateIso = entityDateIso,
        field = field,
        oldValue = oldValue,
        newValue = newValue,
        editedAt = editedAt,
        comment = comment
    )