package ir.kharjyar.data.repository

import androidx.room.withTransaction
import ir.kharjyar.core.common.AppError
import ir.kharjyar.core.common.Result
import ir.kharjyar.core.common.safeSuspendCall
import ir.kharjyar.data.local.KharjYarDatabase
import ir.kharjyar.data.local.entity.CategoryEntity
import ir.kharjyar.data.local.entity.TransactionEntity
import ir.kharjyar.data.local.entity.TransactionSplitEntity
import ir.kharjyar.domain.model.FinancialTransaction
import ir.kharjyar.domain.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KharjYarRepositoryImpl @Inject constructor(
    private val database: KharjYarDatabase
) {
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()

    suspend fun recordTransactionAtomically(tx: FinancialTransaction): Result<Unit> {
        if (tx.amountMinorUnits <= 0L) {
            return Result.Error(
                AppError.ValidationError("amount", "مبلغ تراکنش باید بزرگ‌تر از صفر ریال باشد.")
            )
        }
        if (tx.type == TransactionType.TRANSFER &&
            (tx.targetAccountId.isNullOrBlank() || tx.targetAccountId == tx.accountId)
        ) {
            return Result.Error(
                AppError.ValidationError("targetAccount", "حساب مقصد در انتقال باید متفاوت و معتبر باشد.")
            )
        }
        if (tx.splits.isNotEmpty()) {
            val sumOfSplits = tx.splits.sumOf { it.amountMinorUnits }
            if (sumOfSplits != tx.amountMinorUnits) {
                return Result.Error(
                    AppError.ValidationError(
                        "splits",
                        "مجموع مبالغ بخش‌های تقسیم‌شده با مبلغ کل تراکنش برابر نیست."
                    )
                )
            }
        }

        return safeSuspendCall {
            database.withTransaction {
                val entity = TransactionEntity(
                    id = tx.id,
                    workspaceId = tx.workspaceId,
                    type = tx.type.name,
                    amountMinorUnits = tx.amountMinorUnits,
                    taxMinorUnits = tx.taxMinorUnits,
                    accountId = tx.accountId,
                    targetAccountId = tx.targetAccountId,
                    categoryId = tx.categoryId,
                    subCategoryId = tx.subCategoryId,
                    timestampUtcMillis = tx.timestampUtcMillis,
                    note = tx.note.trim(),
                    tagsCsv = tx.tags.joinToString(","),
                    receiptUri = tx.receiptUri,
                    locationName = tx.locationName,
                    paymentMethod = tx.paymentMethod.name,
                    recurrenceInterval = tx.recurrenceInterval.name,
                    costNature = tx.costNature.name,
                    isDeleted = false,
                    deletedAtUtcMillis = null
                )
                transactionDao.upsertTransaction(entity)
                transactionDao.deleteSplitsForTransaction(tx.id)
                if (tx.splits.isNotEmpty()) {
                    val splitEntities = tx.splits.map { split ->
                        TransactionSplitEntity(
                            id = split.id,
                            transactionId = tx.id,
                            categoryId = split.categoryId,
                            amountMinorUnits = split.amountMinorUnits,
                            note = split.note
                        )
                    }
                    transactionDao.insertSplits(splitEntities)
                }
                transactionDao.clearDraft(tx.workspaceId)
            }
        }
    }

    suspend fun softDeleteTransaction(txId: String): Result<Unit> = safeSuspendCall {
        database.withTransaction {
            transactionDao.softDeleteTransaction(txId, System.currentTimeMillis())
        }
    }

    suspend fun restoreSoftDeletedTransaction(txId: String): Result<Unit> = safeSuspendCall {
        database.withTransaction {
            transactionDao.restoreTransaction(txId)
        }
    }

    suspend fun seedDefaultPersianCategoriesIfNeeded(workspaceId: String, isBusiness: Boolean) {
        database.withTransaction {
            val defaults = if (!isBusiness) {
                listOf(
                    CategoryEntity("cat_food_$workspaceId", workspaceId, null, "خوراک و روزمره", "EXPENSE", "restaurant", "#E53935", 1),
                    CategoryEntity("cat_home_$workspaceId", workspaceId, null, "مسکن و قبوض", "EXPENSE", "home", "#3949AB", 2),
                    CategoryEntity("cat_transport_$workspaceId", workspaceId, null, "حمل‌ونقل و خودرو", "EXPENSE", "directions_car", "#FB8C00", 3),
                    CategoryEntity("cat_health_$workspaceId", workspaceId, null, "سلامت و درمان", "EXPENSE", "medical_services", "#00897B", 4),
                    CategoryEntity("cat_fun_$workspaceId", workspaceId, null, "تفریح و فرهنگ", "EXPENSE", "celebration", "#8E24AA", 5),
                    CategoryEntity("cat_salary_$workspaceId", workspaceId, null, "حقوق و دستمزد", "INCOME", "payments", "#2E7D32", 1),
                    CategoryEntity("cat_invest_$workspaceId", workspaceId, null, "سود سرمایه‌گذاری", "INCOME", "trending_up", "#0277BD", 2)
                )
            } else {
                listOf(
                    CategoryEntity("cat_biz_sales_$workspaceId", workspaceId, null, "فروش کالا و خدمات", "INCOME", "point_of_sale", "#12805C", 1),
                    CategoryEntity("cat_biz_payroll_$workspaceId", workspaceId, null, "حقوق و بیمه پرسنل", "EXPENSE", "groups", "#34469D", 1),
                    CategoryEntity("cat_biz_rent_$workspaceId", workspaceId, null, "اجاره دفتر و انبار", "EXPENSE", "domain", "#7A5900", 2),
                    CategoryEntity("cat_biz_marketing_$workspaceId", workspaceId, null, "تبلیغات و بازاریابی", "EXPENSE", "campaign", "#D93848", 3),
                    CategoryEntity("cat_biz_tax_$workspaceId", workspaceId, null, "مالیات و عوارض قانونی", "EXPENSE", "gavel", "#546E7A", 4)
                )
            }
            categoryDao.insertCategories(defaults)
        }
    }
}
