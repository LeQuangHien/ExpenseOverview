package com.hien.le.expenseoverview.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel(
    protected val dispatchers: CoroutineDispatchers
) {
    protected val vmScope = CoroutineScope(SupervisorJob() + dispatchers.main)
    fun clear() { vmScope.cancel() }
}