package ir.kharjyar.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Serializable
enum class PaymentMethod(val titleFa: String, val titleEn: String) {
    CARD("کارت بانکی", "Card"),
    CASH("نقدی", "Cash"),
    ONLINE("پرداخت آنلاین", "Online"),
    CHECK("چک", "Check")
}

@Serializable
enum class RecurrenceInterval(val titleFa: String, val daysStep: Int) {
    NONE("بدون تکرار", 0),
    DAILY("روزانه", 1),
    WEEKLY("هفتگی", 7),
    MONTHLY("ماهانه", 30),
    YEARLY("سالانه", 365)
}

@Serializable
enum class ExpenseCostNature(val titleFa: String) {
    VARIABLE("متغیر"),
    FIXED("ثابت")
}

@Serializable
data class Account(
    val id: String,
    val workspaceId: String,
    val name: String,
    val type: AccountType,
    val initialBalanceMinorUnits: Long,
    val computedBalanceMinorUnits: Long = initialBalanceMinorUnits,
    val colorHex: String = "#006C51",
    val iconName: String = "account_balance",
    val isArchived: Boolean = false,
    val createdAtUtcMillis: Long
)

@Serializable
data class Category(
    val id: String,
    val workspaceId: String,
    val parentId: String? = null,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String,
    val sortOrder: Int = 0
)

@Serializable
data class TransactionSplitItem(
    val id: String,
    val transactionId: String,
    val categoryId: String,
    val categoryName: String = "",
    val amountMinorUnits: Long,
    val note: String = ""
)

@Serializable
data class FinancialTransaction(
    val id: String,
    val workspaceId: String,
    val type: TransactionType,
    val amountMinorUnits: Long,
    val taxMinorUnits: Long = 0L,
    val accountId: String,
    val targetAccountId: String? = null,
    val categoryId: String? = null,
    val subCategoryId: String? = null,
    val timestampUtcMillis: Long,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val receiptUri: String? = null,
    val locationName: String? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val recurrenceInterval: RecurrenceInterval = RecurrenceInterval.NONE,
    val costNature: ExpenseCostNature = ExpenseCostNature.VARIABLE,
    val splits: List<TransactionSplitItem> = emptyList(),
    val isDeleted: Boolean = false,
    val deletedAtUtcMillis: Long? = null
)

@Serializable
data class BudgetStatus(
    val id: String,
    val workspaceId: String,
    val categoryId: String?,
    val categoryName: String,
    val year: Int,
    val month: Int,
    val limitMinorUnits: Long,
    val spentMinorUnits: Long,
    val previousMonthSpentMinorUnits: Long = 0L,
    val projectedEndOfMonthMinorUnits: Long = 0L
) {
    val usageFraction: Float
        get() = if (limitMinorUnits <= 0L) 0f else (spentMinorUnits.toFloat() / limitMinorUnits.toFloat())

    val isWarning80Percent: Boolean
        get() = usageFraction >= 0.80f && usageFraction < 1.0f

    val isCritical100Percent: Boolean
        get() = usageFraction >= 1.0f
}

@Serializable
data class SavingsGoal(
    val id: String,
    val workspaceId: String,
    val title: String,
    val targetAmountMinorUnits: Long,
    val savedAmountMinorUnits: Long,
    val deadlineUtcMillis: Long,
    val colorHex: String = "#00897B"
) {
    val progressFraction: Float
        get() = if (targetAmountMinorUnits <= 0L) 0f
        else (savedAmountMinorUnits.toFloat() / targetAmountMinorUnits.toFloat()).coerceIn(0f, 1f)

    val isCompleted: Boolean
        get() = savedAmountMinorUnits >= targetAmountMinorUnits && targetAmountMinorUnits > 0L
}

@Serializable
enum class DebtDirection {
    PAYABLE,
    RECEIVABLE
}

@Serializable
data class DebtOrReceivable(
    val id: String,
    val workspaceId: String,
    val personOrCompanyName: String,
    val direction: DebtDirection,
    val totalAmountMinorUnits: Long,
    val paidAmountMinorUnits: Long,
    val dueDateUtcMillis: Long,
    val installmentCount: Int = 1,
    val note: String = ""
) {
    val remainingMinorUnits: Long
        get() = (totalAmountMinorUnits - paidAmountMinorUnits).coerceAtLeast(0L)

    val isFullySettled: Boolean
        get() = remainingMinorUnits == 0L
}

@Serializable
enum class CheckType {
    RECEIVED,
    ISSUED
}

@Serializable
enum class CheckStatus(val titleFa: String) {
    PENDING("در جریان"),
    CLEARED("پاس شده"),
    BOUNCED("برگشتی")
}

@Serializable
data class BusinessCheck(
    val id: String,
    val workspaceId: String,
    val sayyadNumber: String,
    val bankName: String,
    val counterpartName: String,
    val type: CheckType,
    val status: CheckStatus,
    val amountMinorUnits: Long,
    val dueDateUtcMillis: Long,
    val accountId: String
)

@Serializable
data class InvoiceLineItem(
    val title: String,
    val quantity: Int,
    val unitPriceMinorUnits: Long
) {
    val totalMinorUnits: Long
        get() = Math.multiplyExact(quantity.toLong(), unitPriceMinorUnits)
}

@Serializable
data class BusinessInvoice(
    val id: String,
    val workspaceId: String,
    val invoiceNumber: String,
    val customerOrVendorName: String,
    val isSalesInvoice: Boolean,
    val issueDateUtcMillis: Long,
    val dueDateUtcMillis: Long,
    val items: List<InvoiceLineItem>,
    val discountMinorUnits: Long = 0L,
    val vatRateBasisPoints: Int = 1000,
    val isPaid: Boolean = false
) {
    val subtotalMinorUnits: Long
        get() = items.sumOf { it.totalMinorUnits }

    val taxableAmountMinorUnits: Long
        get() = (subtotalMinorUnits - discountMinorUnits).coerceAtLeast(0L)

    val vatAmountMinorUnits: Long
        get() = (taxableAmountMinorUnits * vatRateBasisPoints.toLong()) / 10_000L

    val grandTotalMinorUnits: Long
        get() = taxableAmountMinorUnits + vatAmountMinorUnits
}
