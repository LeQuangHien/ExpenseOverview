package com.hien.le.expenseoverview.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class ClockIos : Clock {
    override fun nowEpochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}