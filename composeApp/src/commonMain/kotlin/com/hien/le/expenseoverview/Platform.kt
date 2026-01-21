package com.hien.le.expenseoverview

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform