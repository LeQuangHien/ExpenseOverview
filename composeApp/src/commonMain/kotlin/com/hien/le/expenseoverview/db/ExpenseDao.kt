package com.hien.le.expenseoverview.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {

    // Daily entry
    @Query("SELECT * FROM daily_entry WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getEntryByDate(dateIso: String): DailyEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entity: DailyEntryEntity)

    @Query("""
        SELECT * FROM daily_entry
        WHERE dateIso >= :fromDateIso AND dateIso <= :toDateIso
        ORDER BY dateIso ASC
    """)
    suspend fun listEntriesInRange(fromDateIso: String, toDateIso: String): List<DailyEntryEntity>

    // Audit
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditEvent(entity: AuditEventEntity)

    @Query("SELECT * FROM audit_event WHERE entityDateIso = :dateIso ORDER BY editedAt DESC")
    suspend fun listAuditByDate(dateIso: String): List<AuditEventEntity>

    @Query("""
        SELECT * FROM audit_event
        WHERE editedAt >= :fromEpochMs AND editedAt <= :toEpochMs
        ORDER BY editedAt DESC
    """)
    suspend fun listAuditInRange(fromEpochMs: Long, toEpochMs: Long): List<AuditEventEntity>

    @Query("DELETE FROM audit_event WHERE editedAt < :cutoffEpochMs")
    suspend fun purgeAuditOlderThan(cutoffEpochMs: Long)
}