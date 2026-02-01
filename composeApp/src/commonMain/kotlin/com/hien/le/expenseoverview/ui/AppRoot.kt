package com.hien.le.expenseoverview.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AppTab(val label: String) {
    ENTRY("Nhập số"),
    SUMMARY("Tổng kết"),
    AUDIT("Log")
}

@Composable
fun AppRoot(
    entryContent: @Composable () -> Unit,
    summaryContent: @Composable () -> Unit,
    auditContent: @Composable () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(AppTab.ENTRY) }

    // Responsive: dùng width size class đơn giản (không cần androidx.window)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp // ngưỡng tablet/desktop-ish

        if (isWide) {
            WideLayout(tab = tab, onTab = { tab = it }) {
                TabContent(tab, entryContent, summaryContent, auditContent)
            }
        } else {
            PhoneLayout(tab = tab, onTab = { tab = it }) {
                TabContent(tab, entryContent, summaryContent, auditContent)
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: AppTab,
    entryContent: @Composable () -> Unit,
    summaryContent: @Composable () -> Unit,
    auditContent: @Composable () -> Unit,
) {
    when (tab) {
        AppTab.ENTRY -> entryContent()
        AppTab.SUMMARY -> summaryContent()
        AppTab.AUDIT -> auditContent()
    }
}

@Composable
private fun WideLayout(
    tab: AppTab,
    onTab: (AppTab) -> Unit,
    content: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            header = {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        ) {
            RailItem(tab, AppTab.ENTRY, Icons.Default.Edit, onTab)
            RailItem(tab, AppTab.SUMMARY, Icons.AutoMirrored.Filled.ShowChart, onTab)
            RailItem(tab, AppTab.AUDIT, Icons.AutoMirrored.Filled.List, onTab)
        }

        VerticalDivider(Modifier.fillMaxHeight().width(1.dp))

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) { content() }
    }
}

@Composable
private fun ColumnScope.RailItem(
    selectedTab: AppTab,
    tab: AppTab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onTab: (AppTab) -> Unit
) {
    NavigationRailItem(
        selected = selectedTab == tab,
        onClick = { onTab(tab) },
        icon = { Icon(icon, contentDescription = tab.label) },
        label = { Text(tab.label) },
        alwaysShowLabel = true
    )
}

@Composable
private fun PhoneLayout(
    tab: AppTab,
    onTab: (AppTab) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomItem(tab, AppTab.ENTRY, Icons.Default.Edit, onTab)
                BottomItem(tab, AppTab.SUMMARY, Icons.AutoMirrored.Filled.ShowChart, onTab)
                BottomItem(tab, AppTab.AUDIT, Icons.AutoMirrored.Filled.List, onTab)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    selectedTab: AppTab,
    tab: AppTab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onTab: (AppTab) -> Unit
) {
    NavigationBarItem(
        selected = selectedTab == tab,
        onClick = { onTab(tab) },
        icon = { Icon(icon, contentDescription = tab.label) },
        label = { Text(tab.label) }
    )
}
