package com.hien.le.expenseoverview.presentation.common

import kotlin.math.abs

object MoneyFormatter {
    // "12,30 €" theo kiểu Đức
    fun centsToDeEuro(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val a = abs(cents)
        val euros = a / 100
        val c = (a % 100).toString().padStart(2, '0')
        return "$sign$euros,$c €"
    }
}