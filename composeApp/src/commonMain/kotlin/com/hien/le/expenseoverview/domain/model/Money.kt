package com.hien.le.expenseoverview.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class Cents(val value: Long) {
    init { require(value >= 0) { "Cents must be >= 0" } }
}