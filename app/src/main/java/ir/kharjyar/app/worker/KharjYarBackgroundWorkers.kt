package ir.kharjyar.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.kharjyar.data.local.KharjYarDatabase
import java.util.concurrent.TimeUnit

@HiltWorker
class KharjYarDailyMaintenanceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: KharjYarDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val thirtyDaysAgoUtc = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            database.transactionDao().purgeExpiredTrashTransactions(thirtyDaysAgoUtc)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "kharjyar_daily_maintenance_work"

        fun schedulePeriodicWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<KharjYarDailyMaintenanceWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
