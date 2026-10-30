package ir.kharjyar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.kharjyar.data.local.dao.AccountDao
import ir.kharjyar.data.local.dao.CategoryDao
import ir.kharjyar.data.local.dao.PlanningAndBusinessDao
import ir.kharjyar.data.local.dao.TransactionDao
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

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        TransactionSplitEntity::class,
        DraftTransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        DebtReceivableEntity::class,
        BusinessCheckEntity::class,
        BusinessInvoiceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KharjYarDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun planningAndBusinessDao(): PlanningAndBusinessDao

    companion object {
        const val DATABASE_NAME = "kharjyar_financial.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE transactions ADD COLUMN cost_nature TEXT NOT NULL DEFAULT 'VARIABLE'"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `draft_transactions` (" +
                        "`workspaceId` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                        "`amount_expression` TEXT NOT NULL, `account_id` TEXT, " +
                        "`category_id` TEXT, `note` TEXT NOT NULL, " +
                        "`tags_csv` TEXT NOT NULL, `updated_at_utc` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`workspaceId`))"
                )
            }
        }
    }
}
