package ir.kharjyar.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.money.ExpressionEvaluator
import ir.kharjyar.core.money.MoneyFormatter

@Composable
fun CustomCalculatorKeypad(
    currentExpression: String,
    onExpressionChange: (String) -> Unit,
    onConfirmAmount: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("۷", "۸", "۹", "÷"),
        listOf("۴", "۵", "۶", "×"),
        listOf("۱", "۲", "۳", "-"),
        listOf("۰۰", "۰", "⌫", "+")
    )

    val liveEvaluated = ExpressionEvaluator.evaluateToLong(currentExpression)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (liveEvaluated != null && currentExpression.any { it in listOf('+', '-', '×', '÷', '*', '/') }) {
            Text(
                text = "= ${MoneyFormatter.toPersianDigits("%,d".format(liveEvaluated))}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { key ->
                    val isOperator = key in listOf("÷", "×", "-", "+", "⌫")
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            when (key) {
                                "⌫" -> {
                                    if (currentExpression.isNotEmpty()) {
                                        onExpressionChange(currentExpression.dropLast(1))
                                    }
                                }
                                else -> onExpressionChange(currentExpression + key)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isOperator) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}
