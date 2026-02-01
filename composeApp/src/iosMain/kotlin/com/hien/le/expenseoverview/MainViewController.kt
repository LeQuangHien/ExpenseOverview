package com.hien.le.expenseoverview

import androidx.compose.ui.window.ComposeUIViewController
import com.hien.le.expenseoverview.ui.AppEntryPoint

fun MainViewController() = ComposeUIViewController {
    val container = IosAppContainer()
    AppEntryPoint(container)
}