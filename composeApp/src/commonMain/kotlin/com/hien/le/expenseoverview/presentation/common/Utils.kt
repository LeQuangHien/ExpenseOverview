package com.hien.le.expenseoverview.presentation.common

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object Utils {
    @OptIn(ExperimentalUuidApi::class)
    fun generateUuid(): String =
        Uuid.random().toString()
}