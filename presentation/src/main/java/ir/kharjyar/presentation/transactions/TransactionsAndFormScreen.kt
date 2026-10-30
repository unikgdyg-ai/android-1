package ir.kharjyar.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.date.JalaliDateConverter
import ir.kharjyar.core.designsystem.components.SpringSwipeableItem
import ir.kharjyar.core.designsystem.components.StaggeredAnimatedItem
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.designsystem.theme.KharjYarThemeExtensions
import ir.kharjyar.core.money.ExpressionEvaluator
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CalendarType
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.FinancialTransaction
import ir.kharjyar.domain.model.PaymentMethod
import ir.kharjyar.domain.model.RecurrenceInterval
import ir.kharjyar.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsTabScreen(
    currency: CurrencyUnit,
    calendarType: CalendarType,
    privacyMode: Boolean,
    onOpenNewTransactionForm: () -> Unit,
    onTransactionDeletedWithUndo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTypeFilter by rememberSaveable { mutableStateOf<TransactionType?>(null) }
    val financialColors = KharjYarThemeExtensions.financialColors

    val sampleTransactions = remember {
        mutableStateListOf(
            FinancialTransaction(
                id = "tx_101",
                workspaceId = "workspace_personal_default",
                type = TransactionType.EXPENSE,
                amountMinorUnits = 4_850_000L,
                accountId = "acc_card_1",
                categoryId = "خوراک و روزمره",
                timestampUtcMillis = System.currentTimeMillis() - 3_600_000L,
                note = "خرید هفتگی فروشگاه و میوه",
                tags = listOf("روزمره", "خانه"),
                paymentMethod = PaymentMethod.CARD
            ),
            FinancialTransaction(
                id = "tx_102",
                workspaceId = "workspace_personal_default",
                type = TransactionType.INCOME,
                amountMinorUnits = 620_000_000L,
                accountId = "acc_bank_1",
                categoryId = "حقوق و دستمزد",
                timestampUtcMillis = System.currentTimeMillis() - 86_400_000L,
                note = "واریز حقوق ماهانه و پاداش پروژه",
                tags = listOf("حقوق", "ماهانه"),
                paymentMethod = PaymentMethod.ONLINE,
                recurrenceInterval = RecurrenceInterval.MONTHLY
            ),
            FinancialTransaction(
                id = "tx_103",
                workspaceId = "workspace_personal_default",
                type = TransactionType.EXPENSE,
                amountMinorUnits = 85_000_000L,
                accountId = "acc_bank_1",
                categoryId = "مسکن و قبوض",
                timestampUtcMillis = System.currentTimeMillis() - 172_800_000L,
                note = "پرداخت اجاره منزل و شارژ ساختمان",
                tags = listOf("اجاره", "ثابت"),
                paymentMethod = PaymentMethod.ONLINE,
                recurrenceInterval = RecurrenceInterval.MONTHLY
            )
        )
    }

    val filteredList = sampleTransactions.filter { tx ->
        !tx.isDeleted &&
            (selectedTypeFilter == null || tx.type == selectedTypeFilter) &&
            (searchQuery.isBlank() || tx.note.contains(searchQuery, ignoreCase = true) ||
                tx.tags.any { it.contains(searchQuery, ignoreCase = true) })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو در توضیح، تگ یا دسته...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("همه") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                    label = { Text("هزینه‌ها") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.INCOME,
                    onClick = { selectedTypeFilter = TransactionType.INCOME },
                    label = { Text("درآمدها") }
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.TRANSFER,
                    onClick = { selectedTypeFilter = TransactionType.TRANSFER },
                    label = { Text("انتقال بین حساب") }
                )
            }
        }

        itemsIndexed(filteredList, key = { _, item -> item.id }) { index, tx ->
            StaggeredAnimatedItem(index = index) {
                SpringSwipeableItem(
                    onEdit = onOpenNewTransactionForm,
                    onDelete = {
                        val idx = sampleTransactions.indexOfFirst { it.id == tx.id }
                        if (idx >= 0) {
                            sampleTransactions[idx] = tx.copy(isDeleted = true)
                            onTransactionDeletedWithUndo(tx.id)
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.categoryId ?: "تراکنش مالی",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tx.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${JalaliDateConverter.formatUtcTimestamp(tx.timestampUtcMillis, calendarType)} • ${tx.paymentMethod.titleFa}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val amountColor = when (tx.type) {
                                TransactionType.INCOME -> financialColors.income
                                TransactionType.EXPENSE -> financialColors.expense
                                TransactionType.TRANSFER -> financialColors.transfer
                            }
                            Text(
                                text = MoneyFormatter.format(
                                    money = Money(tx.amountMinorUnits),
                                    currency = currency,
                                    privacyMode = privacyMode
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = amountColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionFormScreen(
    currency: CurrencyUnit,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var txType by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var amountExpression by rememberSaveable { mutableStateOf("250000") }
    var selectedCategory by rememberSaveable { mutableStateOf("خوراک و روزمره") }
    var selectedPaymentMethod by rememberSaveable { mutableStateOf(PaymentMethod.CARD) }
    var selectedRecurrence by rememberSaveable { mutableStateOf(RecurrenceInterval.NONE) }
    var note by rememberSaveable { mutableStateOf("") }
    var tagsInput by rememberSaveable { mutableStateOf("روزمره، خرید") }
    var splitEnabled by rememberSaveable { mutableStateOf(false) }
    var splitPartsCount by rememberSaveable { mutableStateOf(2) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val evaluatedDisplayAmount = ExpressionEvaluator.evaluateToLong(amountExpression)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "ثبت تراکنش جدید (همراه با ذخیره خودکار پیش‌نویس)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = txType == type,
                        onClick = { txType = type },
                        label = {
                            Text(
                                when (type) {
                                    TransactionType.EXPENSE -> "ثبت هزینه"
                                    TransactionType.INCOME -> "ثبت درآمد"
                                    TransactionType.TRANSFER -> "انتقال بین حساب"
                                }
                            )
                        }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = amountExpression,
                onValueChange = {
                    amountExpression = it
                    errorMessage = null
                },
                label = { Text("مبلغ یا عبارت ریاضی (${currency.symbolFa})") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CustomCalculatorKeypad(
                currentExpression = amountExpression,
                onExpressionChange = { amountExpression = it },
                onConfirmAmount = {}
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CallSplit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تقسیم تراکنش به چند دسته (Split)", style = MaterialTheme.typography.titleSmall)
                        }
                        Switch(checked = splitEnabled, onCheckedChange = { splitEnabled = it })
                    }

                    if (splitEnabled && evaluatedDisplayAmount != null && evaluatedDisplayAmount > 0L) {
                        val totalMoney = Money.fromDisplayValue(evaluatedDisplayAmount, currency)
                        val splitShares = totalMoney.splitIntoParts(splitPartsCount)
                        Text(
                            text = "سهم هر بخش (${splitPartsCount} دسته بدون خطای گرد کردن):",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        splitShares.forEachIndexed { idx, share ->
                            Text(
                                text = "• بخش ${idx + 1}: ${MoneyFormatter.format(share, currency)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("توضیحات تراکنش") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("تگ‌ها (جداشده با ویرگول)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Button(
                onClick = {
                    val amount = evaluatedDisplayAmount
                    if (amount == null || amount <= 0L) {
                        errorMessage = "مبلغ واردشده نامعتبر است؛ مبلغ باید بزرگ‌تر از صفر باشد."
                    } else {
                        val money = Money.fromDisplayValue(amount, currency)
                        onSaved("تراکنش به مبلغ ${MoneyFormatter.format(money, currency)} با موفقیت ثبت شد")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = KharjYarDimens.MinTouchTarget)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره اتمیک تراکنش")
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
