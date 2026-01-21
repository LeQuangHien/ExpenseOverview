package com.hien.le.expenseoverview.presentation.common

object MoneyInput {
    fun parseToCents(text: String): Long? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0L
        val normalized = trimmed.replace(',', '.')
        val parts = normalized.split('.')
        if (parts.size > 2) return null
        val euros = parts[0].toLongOrNull() ?: return null
        val cents = when (parts.size) {
            1 -> 0
            else -> {
                val c = parts[1]
                if (c.length > 2) return null
                (c.padEnd(2, '0')).toIntOrNull() ?: return null
            }
        }
        if (euros < 0 || cents < 0) return null
        return euros * 100L + cents.toLong()
    }
}