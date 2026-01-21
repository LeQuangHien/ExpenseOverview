package com.hien.le.expenseoverview.platform

class ClockAndroid : Clock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}