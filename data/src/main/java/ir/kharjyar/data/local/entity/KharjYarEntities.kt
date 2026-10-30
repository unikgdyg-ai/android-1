package ir.kharjyar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["workspace_id"]), Index(value = ["is_archived"])]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "initial_balance_minor") val initialBalanceMinorUnits: Long,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "icon_name") val iconName: String,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at_utc") val createdAtUtcMillis: Long
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workspace_id", "type"]),
        Index(value = ["parent_id"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "parent_id") val parentId: String? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "icon_name") val iconName: String,
    @ColumnInfo(name = "color_hex") val colorHex: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["target_account_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["workspace_id", "timestamp_utc"]),
        Index(value = ["account_id"]),
        Index(value = ["target_account_id"]),
        Index(value = ["category_id"]),
        Index(value = ["is_deleted", "deleted_at_utc"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "amount_minor") val amountMinorUnits: Long,
    @ColumnInfo(name = "tax_minor") val taxMinorUnits: Long = 0L,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "target_account_id") val targetAccountId: String? = null,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "sub_category_id") val subCategoryId: String? = null,
    @ColumnInfo(name = "timestamp_utc") val timestampUtcMillis: Long,
    @ColumnInfo(name = "note") val note: String = "",
    @ColumnInfo(name = "tags_csv") val tagsCsv: String = "",
    @ColumnInfo(name = "receipt_uri") val receiptUri: String? = null,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "payment_method") val paymentMethod: String = "CARD",
    @ColumnInfo(name = "recurrence_interval") val recurrenceInterval: String = "NONE",
    @ColumnInfo(name = "cost_nature") val costNature: String = "VARIABLE",
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at_utc") val deletedAtUtcMillis: Long? = null
)

@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["category_id"])
    ]
)
data class TransactionSplitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "transaction_id") val transactionId: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "amount_minor") val amountMinorUnits: Long,
    @ColumnInfo(name = "note") val note: String = ""
)

@Entity(tableName = "draft_transactions")
data class DraftTransactionEntity(
    @PrimaryKey val workspaceId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "amount_expression") val amountExpression: String,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "note") val note: String,
    @ColumnInfo(name = "tags_csv") val tagsCsv: String,
    @ColumnInfo(name = "updated_at_utc") val updatedAtUtcMillis: Long
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["workspace_id", "year", "month", "category_id"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "month") val month: Int,
    @ColumnInfo(name = "limit_minor") val limitMinorUnits: Long
)

@Entity(
    tableName = "savings_goals",
    indices = [Index(value = ["workspace_id"])]
)
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "target_amount_minor") val targetAmountMinorUnits: Long,
    @ColumnInfo(name = "saved_amount_minor") val savedAmountMinorUnits: Long,
    @ColumnInfo(name = "deadline_utc") val deadlineUtcMillis: Long,
    @ColumnInfo(name = "color_hex") val colorHex: String
)

@Entity(
    tableName = "debts_receivables",
    indices = [Index(value = ["workspace_id", "direction"]), Index(value = ["due_date_utc"])]
)
data class DebtReceivableEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "person_name") val personOrCompanyName: String,
    @ColumnInfo(name = "direction") val direction: String,
    @ColumnInfo(name = "total_amount_minor") val totalAmountMinorUnits: Long,
    @ColumnInfo(name = "paid_amount_minor") val paidAmountMinorUnits: Long,
    @ColumnInfo(name = "due_date_utc") val dueDateUtcMillis: Long,
    @ColumnInfo(name = "installment_count") val installmentCount: Int,
    @ColumnInfo(name = "note") val note: String
)

@Entity(
    tableName = "business_checks",
    indices = [Index(value = ["workspace_id", "status"]), Index(value = ["due_date_utc"])]
)
data class BusinessCheckEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "sayyad_number") val sayyadNumber: String,
    @ColumnInfo(name = "bank_name") val bankName: String,
    @ColumnInfo(name = "counterpart_name") val counterpartName: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "amount_minor") val amountMinorUnits: Long,
    @ColumnInfo(name = "due_date_utc") val dueDateUtcMillis: Long,
    @ColumnInfo(name = "account_id") val accountId: String
)

@Entity(
    tableName = "business_invoices",
    indices = [Index(value = ["workspace_id", "issue_date_utc"])]
)
data class BusinessInvoiceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workspace_id") val workspaceId: String,
    @ColumnInfo(name = "invoice_number") val invoiceNumber: String,
    @ColumnInfo(name = "counterpart_name") val customerOrVendorName: String,
    @ColumnInfo(name = "is_sales") val isSalesInvoice: Boolean,
    @ColumnInfo(name = "issue_date_utc") val issueDateUtcMillis: Long,
    @ColumnInfo(name = "due_date_utc") val dueDateUtcMillis: Long,
    @ColumnInfo(name = "items_json") val itemsJson: String,
    @ColumnInfo(name = "discount_minor") val discountMinorUnits: Long,
    @ColumnInfo(name = "vat_rate_bp") val vatRateBasisPoints: Int,
    @ColumnInfo(name = "is_paid") val isPaid: Boolean
)
