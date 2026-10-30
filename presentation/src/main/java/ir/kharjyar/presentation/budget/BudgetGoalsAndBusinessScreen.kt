package ir.kharjyar.presentation.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.designsystem.theme.KharjYarThemeExtensions
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.BudgetStatus
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.DebtDirection
import ir.kharjyar.domain.model.DebtOrReceivable
import ir.kharjyar.domain.model.SavingsGoal

@Composable
fun BudgetGoalsAndDebtTabScreen(
    currency: CurrencyUnit,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val financialColors = KharjYarThemeExtensions.financialColors

    val budgets = remember {
        listOf(
            BudgetStatus("b1", "ws", null, "بودجه کل ماهانه", 1405, 7, 200_000_000L, 167_000_000L, 150_000_000L),
            BudgetStatus("b2", "ws", "cat_food", "خوراک و روزمره", 1405, 7, 50_000_000L, 42_000_000L, 35_500_000L),
            BudgetStatus("b3", "ws", "cat_fun", "تفریح و فرهنگ", 1405, 7, 10_000_000L, 10_500_000L, 8_000_000L)
        )
    }

    val goals = remember {
        listOf(
            SavingsGoal("g1", "ws", "صندوق ذخیره اضطراری (۶ ماه)", 600_000_000L, 420_000_000L, System.currentTimeMillis()),
            SavingsGoal("g2", "ws", "ارتقای لپ‌تاپ کاری", 850_000_000L, 850_000_000L, System.currentTimeMillis())
        )
    }

    val debts = remember {
        listOf(
            DebtOrReceivable("d1", "ws", "وام مسکن بانک مسکن", DebtDirection.PAYABLE, 1_200_000_000L, 450_000_000L, System.currentTimeMillis(), 24),
            DebtOrReceivable("d2", "ws", "طلب پروژه طراحی رابط کاربری", DebtDirection.RECEIVABLE, 180_000_000L, 90_000_000L, System.currentTimeMillis(), 2)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "بودجه‌بندی ماهانه (هشدار در ۸۰٪ و ۱۰۰٪)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(budgets, key = { it.id }) { budget ->
            val animProgress by animateFloatAsState(
                targetValue = budget.usageFraction.coerceIn(0f, 1f),
                animationSpec = tween(750),
                label = "BudgetProgress"
            )
            val barColor = when {
                budget.isCritical100Percent -> financialColors.budgetCritical100
                budget.isWarning80Percent -> financialColors.budgetWarning80
                else -> financialColors.budgetSafe
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(budget.categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${(budget.usageFraction * 100).toInt()}٪",
                            color = barColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animProgress },
                        color = barColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مصرف‌شده: ${MoneyFormatter.format(Money(budget.spentMinorUnits), currency, privacyMode = privacyMode)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "سقف: ${MoneyFormatter.format(Money(budget.limitMinorUnits), currency, privacyMode = privacyMode)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "اهداف پس‌انداز",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(goals, key = { it.id }) { goal ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (goal.isCompleted) financialColors.incomeContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (goal.isCompleted) "تکمیل شد 🎉" else "${(goal.progressFraction * 100).toInt()}٪")
                    }
                    LinearProgressIndicator(
                        progress = { goal.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Text(
                        text = "${MoneyFormatter.format(Money(goal.savedAmountMinorUnits), currency, privacyMode = privacyMode)} از ${MoneyFormatter.format(Money(goal.targetAmountMinorUnits), currency, privacyMode = privacyMode)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "بدهی‌ها و مطالبات (اقساط و سررسید)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(debts, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${if (item.direction == DebtDirection.PAYABLE) "بدهی به:" else "طلب از:"} ${item.personOrCompanyName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "باقی‌مانده تسویه: ${MoneyFormatter.format(Money(item.remainingMinorUnits), currency, privacyMode = privacyMode)} (${item.installmentCount} قسط)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}
