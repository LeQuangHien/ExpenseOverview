package com.hien.le.expenseoverview.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hien.le.expenseoverview.presentation.common.MoneyFormatter
import com.hien.le.expenseoverview.presentation.common.MoneyInput

@Composable
fun MoneyKeypadInput(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyColor: Color = MaterialTheme.colorScheme.primary // ✅ NEW
) {
    val cents = remember(text) { MoneyInput.parseToCents(text) ?: 0L }
    val formatted = remember(cents) { MoneyFormatter.centsToDeEuro(cents) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = {}, // readOnly (keypad only)
            readOnly = true,
            label = { Text(label) },
            supportingText = { Text(formatted) },
            modifier = Modifier.fillMaxWidth()
        )

        MoneyKeypad(
            keyColor = keyColor, // ✅ pass down
            onDigit = { d -> onTextChange(appendDigit(text, d)) },
            onComma = { onTextChange(appendComma(text)) },
            onBackspace = { onTextChange(backspace(text)) },
            onClear = { onTextChange("") }
        )
    }
}

@Composable
private fun MoneyKeypad(
    keyColor: Color, // ✅ NEW
    onDigit: (Char) -> Unit,
    onComma: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key("1", keyColor) { onDigit('1') }
            Key("2", keyColor) { onDigit('2') }
            Key("3", keyColor) { onDigit('3') }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key("4", keyColor) { onDigit('4') }
            Key("5", keyColor) { onDigit('5') }
            Key("6", keyColor) { onDigit('6') }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key("7", keyColor) { onDigit('7') }
            Key("8", keyColor) { onDigit('8') }
            Key("9", keyColor) { onDigit('9') }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key(",", keyColor) { onComma() }
            Key("0", keyColor) { onDigit('0') }
            Key("⌫", keyColor) { onBackspace() }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = keyColor
                )
            ) {
                Text("Xóa")
            }
        }
    }
}

@Composable
private fun RowScope.Key(
    label: String,
    keyColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = keyColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(label)
    }
}

// Logic: chỉ cho 1 dấu phẩy, và tối đa 2 số sau phẩy.
private fun appendDigit(cur: String, d: Char): String {
    if (cur.isEmpty() && d == '0') return "0" // keep 0
    val next = cur + d
    val parts = next.split(',')
    if (parts.size == 1) return next.take(9) // limit length
    val decimals = parts.getOrNull(1).orEmpty()
    if (decimals.length > 2) return cur
    return next.take(12)
}

private fun appendComma(cur: String): String {
    if (cur.isEmpty()) return "0,"
    if (cur.contains(',')) return cur
    return "$cur,"
}

private fun backspace(cur: String): String =
    if (cur.isEmpty()) "" else cur.dropLast(1)