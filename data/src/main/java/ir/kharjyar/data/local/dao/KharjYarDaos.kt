package ir.kharjyar.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import ir.kharjyar.data.local.entity.AccountEntity
import ir.kharjyar.data.local.entity.BudgetEntity
import ir.kharjyar.data.local.entity.BusinessCheckEntity
import ir.kharjyar.data.local.entity.BusinessInvoiceEntity
import ir.kharjyar.data.local.entity.CategoryEntity
import ir.kharjyar.data.local.entity.DebtReceivableEntity
import ir.kharjyar.data.local.entity.DraftTransactionEntity
import ir.kharjyar.data.local.entity.SavingsGoalEntity
import ir.kharjyar.data.local.entity.TransactionEntity
import ir.kharjyar.data.local.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow

data class AccountWithComputedBalanceRow(
    @Embedded val account: AccountEntity,
    @ColumnInfo(name = "computed_balance_minor") val computedBalanceMinorUnits: Long
)

data class CategorySpendSummaryRow(
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "total_minor") val totalMinorUnits: Long
)

@Dao
interface AccountDao {

    @Query(
        "SELECT a.*, " +
            "(a.initial_balance_minor " +
            " + COALESCE((SELECT SUM(t.amount_minor) FROM transactions t WHERE t.account_id = a.id AND t.type = 'INCOME' AND t.is_deleted = 0), 0) " +
            " - COALESCE((SELECT SUM(t.amount_minor + t.tax_minor) FROM transactions t WHERE t.account_id = a.id AND t.type = 'EXPENSE' AND t.is_deleted = 0), 0) " +
            " - COALESCE((SELECT SUM(t.amount_minor) FROM transactions t WHERE t.account_id = a.id AND t.type = 'TRANSFER' AND t.is_deleted = 0), 0) " +
            " + COALESCE((SELECT SUM(t.amount_minor) FROM transactions t WHERE t.target_account_id = a.id AND t.type = 'TRANSFER' AND t.is_deleted = 0), 0) " +
            ") AS computed_balance_minor " +
            "FROM accounts a WHERE a.workspace_id = :workspaceId AND a.is_archived = 0 ORDER BY a.created_at_utc ASC"
    )
    fun observeAccountsWithBalances(workspaceId: String): Flow<List<AccountWithComputedBalanceRow>>

    @Upsert
    suspend fun upsertAccount(account: AccountEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE workspace_id = :workspaceId ORDER BY sort_order ASC, name ASC")
    fun observeCategories(workspaceId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)
}

@Dao
interface TransactionDao {

    @Query(
        "SELECT * FROM transactions " +
            "WHERE workspace_id = :workspaceId AND is_deleted = 0 " +
            "AND (:fromUtc IS NULL OR timestamp_utc >= :fromUtc) " +
            "AND (:toUtc IS NULL OR timestamp_utc <= :toUtc) " +
            "AND (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (:accountId IS NULL OR account_id = :accountId OR target_account_id = :accountId) " +
            "AND (:searchQuery = '' OR note LIKE '%' || :searchQuery || '%' OR tags_csv LIKE '%' || :searchQuery || '%') " +
            "ORDER BY timestamp_utc DESC"
    )
    fun observeFilteredTransactions(
        workspaceId: String,
        fromUtc: Long? = null,
        toUtc: Long? = null,
        categoryId: String? = null,
        accountId: String? = null,
        searchQuery: String = ""
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE workspace_id = :workspaceId AND is_deleted = 1 ORDER BY deleted_at_utc DESC")
    fun observeTrashBinTransactions(workspaceId: String): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<TransactionSplitEntity>)

    @Query("DELETE FROM transaction_splits WHERE transaction_id = :transactionId")
    suspend fun deleteSplitsForTransaction(transactionId: String)

    @Query("UPDATE transactions SET is_deleted = 1, deleted_at_utc = :deletedAtUtc WHERE id = :txId")
    suspend fun softDeleteTransaction(txId: String, deletedAtUtc: Long)

    @Query("UPDATE transactions SET is_deleted = 0, deleted_at_utc = NULL WHERE id = :txId")
    suspend fun restoreTransaction(txId: String)

    @Query("DELETE FROM transactions WHERE is_deleted = 1 AND deleted_at_utc < :thresholdUtc")
    suspend fun purgeExpiredTrashTransactions(thresholdUtc: Long): Int

    @Query(
        "SELECT category_id, COALESCE(SUM(amount_minor + tax_minor), 0) AS total_minor " +
            "FROM transactions WHERE workspace_id = :workspaceId AND type = 'EXPENSE' AND is_deleted = 0 " +
            "AND timestamp_utc BETWEEN :fromUtc AND :toUtc GROUP BY category_id ORDER BY total_minor DESC"
    )
    fun observeCategoryExpenseTotals(
        workspaceId: String,
        fromUtc: Long,
        toUtc: Long
    ): Flow<List<CategorySpendSummaryRow>>

    @Upsert
    suspend fun saveDraft(draft: DraftTransactionEntity)

    @Query("SELECT * FROM draft_transactions WHERE workspaceId = :workspaceId LIMIT 1")
    suspend fun getDraft(workspaceId: String): DraftTransactionEntity?

    @Query("DELETE FROM draft_transactions WHERE workspaceId = :workspaceId")
    suspend fun clearDraft(workspaceId: String)
}

@Dao
interface PlanningAndBusinessDao {
    @Query("SELECT * FROM budgets WHERE workspace_id = :workspaceId AND year = :year AND month = :month")
    fun observeBudgetsForMonth(workspaceId: String, year: Int, month: Int): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity)

    @Query("SELECT * FROM savings_goals WHERE workspace_id = :workspaceId ORDER BY deadline_utc ASC")
    fun observeSavingsGoals(workspaceId: String): Flow<List<SavingsGoalEntity>>

    @Upsert
    suspend fun upsertSavingsGoal(goal: SavingsGoalEntity)

    @Query("SELECT * FROM debts_receivables WHERE workspace_id = :workspaceId ORDER BY due_date_utc ASC")
    fun observeDebtsAndReceivables(workspaceId: String): Flow<List<DebtReceivableEntity>>

    @Upsert
    suspend fun upsertDebtOrReceivable(item: DebtReceivableEntity)

    @Query("SELECT * FROM business_checks WHERE workspace_id = :workspaceId ORDER BY due_date_utc ASC")
    fun observeBusinessChecks(workspaceId: String): Flow<List<BusinessCheckEntity>>

    @Upsert
    suspend fun upsertBusinessCheck(check: BusinessCheckEntity)

    @Query("SELECT * FROM business_invoices WHERE workspace_id = :workspaceId ORDER BY issue_date_utc DESC")
    fun observeBusinessInvoices(workspaceId: String): Flow<List<BusinessInvoiceEntity>>

    @Upsert
    suspend fun upsertBusinessInvoice(invoice: BusinessInvoiceEntity)
}
