package com.hien.le.expenseoverview.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_event",
    indices = [
        Index(value = ["entityDateIso"]),
        Index(value = ["editedAt"])
    ]
)
data class AuditEventEntity(
    @PrimaryKey val id: String,
    val entityDateIso: String,
    val field: String,      // bargeld | karte | expense | note
    val oldValue: String,
    val newValue: String,
    val editedAt: Long,
    val comment: String?
)