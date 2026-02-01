package com.hien.le.expenseoverview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable
fun DateQuickPicker(
    selectedDateIso: String,
    onSelectDateIso: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val todayIso = remember { today.toString() }
    val yesterdayIso = remember { (today - DatePeriod(days = 1)).toString() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = { onSelectDateIso(todayIso) },
            label = { Text("Hôm nay") }
        )
        AssistChip(
            onClick = { onSelectDateIso(yesterdayIso) },
            label = { Text("Hôm qua") }
        )
        OutlinedButton(onClick = { showDialog = true }) {
            Text("Chọn ngày")
        }

        Spacer(Modifier.weight(1f))
        Text(selectedDateIso.ifBlank { todayIso }, style = MaterialTheme.typography.labelLarge)
    }

    if (showDialog) {
        // ✅ Nếu selectedDateIso parse fail/placeholder -> vẫn mở ở today
        val selectedInitial = remember(selectedDateIso) {
            runCatching { LocalDate.parse(selectedDateIso) }
                .getOrElse { today }
        }

        CalendarDatePickerDialog(
            today = today,
            initialSelected = selectedInitial,
            onDismiss = { showDialog = false },
            onConfirm = { iso ->
                onSelectDateIso(iso)
                showDialog = false
            }
        )
    }
}

@Composable
private fun CalendarDatePickerDialog(
    today: LocalDate,
    initialSelected: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf(initialSelected) }

    // ✅ Month anchor luôn lấy theo today (đúng yêu cầu bạn)
    // (user có thể đổi tháng bằng nút prev/next)
    var monthAnchor by remember { mutableStateOf(LocalDate(today.year, today.monthNumber, 1)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn ngày") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MonthHeader(
                    monthAnchor = monthAnchor,
                    onPrevMonth = { monthAnchor = monthAnchor.minusMonths(1) },
                    onNextMonth = { monthAnchor = monthAnchor.plusMonths(1) }
                )

                WeekdayHeader()

                MonthGrid(
                    monthAnchor = monthAnchor,
                    selected = selected,
                    today = today,
                    onSelect = { selected = it }
                )

                Text("Đã chọn: $selected", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toString()) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}

@Composable
private fun MonthHeader(
    monthAnchor: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevMonth) { Text("‹") }

        Spacer(Modifier.weight(1f))

        Text(
            text = "${monthAnchor.year}-${monthAnchor.monthNumber.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onNextMonth) { Text("›") }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    Row(Modifier.fillMaxWidth()) {
        labels.forEach { d ->
            Text(
                text = d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MonthGrid(
    monthAnchor: LocalDate,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    val first = monthAnchor
    val daysInMonth = first.daysInMonth()
    val startOffset = mondayFirstOffset(first.dayOfWeek) // 0..6

    val totalCells = 42
    val dayNumbers = (0 until totalCells).map { cell ->
        val dayIndex = cell - startOffset + 1
        if (dayIndex in 1..daysInMonth) dayIndex else null
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (col in 0 until 7) {
                    val idx = row * 7 + col
                    val day = dayNumbers[idx]
                    val date = day?.let { LocalDate(first.year, first.monthNumber, it) }

                    DayCell(
                        dayNumber = day,
                        isSelected = (date != null && date == selected),
                        isToday = (date != null && date == today),
                        onClick = { if (date != null) onSelect(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    dayNumber: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.small

    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val fg = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    // ✅ border để phân biệt rõ "today" và "selected"
    val borderColor = when {
        isSelected && isToday -> MaterialTheme.colorScheme.primary // highlight mạnh nhất
        isToday -> MaterialTheme.colorScheme.outline               // today nhưng chưa chọn
        else -> null
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(bg, shape)
            .then(
                if (borderColor != null) Modifier.border(
                    width = 2.dp,
                    color = borderColor,
                    shape = shape
                ) else Modifier
            )
            .clickable(enabled = dayNumber != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber?.toString() ?: "",
            color = fg,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Monday-first offset: Monday->0 ... Sunday->6 */
private fun mondayFirstOffset(dow: DayOfWeek): Int = when (dow) {
    DayOfWeek.MONDAY -> 0
    DayOfWeek.TUESDAY -> 1
    DayOfWeek.WEDNESDAY -> 2
    DayOfWeek.THURSDAY -> 3
    DayOfWeek.FRIDAY -> 4
    DayOfWeek.SATURDAY -> 5
    DayOfWeek.SUNDAY -> 6
}

private fun LocalDate.plusMonths(n: Int): LocalDate = this + DatePeriod(months = n)
private fun LocalDate.minusMonths(n: Int): LocalDate = this + DatePeriod(months = -n)

private fun LocalDate.daysInMonth(): Int {
    val firstOfMonth = LocalDate(year, monthNumber, 1)
    val firstNextMonth = firstOfMonth + DatePeriod(months = 1)
    val lastDay = firstNextMonth - DatePeriod(days = 1)
    return lastDay.dayOfMonth
}