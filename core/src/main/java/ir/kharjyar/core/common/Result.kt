package ir.kharjyar.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * مدیریت ساختارمند خطاها در تمام لایه‌ها برای جلوگیری کامل از Crash
 */
sealed interface AppError {
    data class ValidationError(val field: String, val messageResOrText: String) : AppError
    data class DatabaseError(val causeMessage: String) : AppError
    data class InsufficientBalanceError(val accountName: String) : AppError
    data class BackupRestoreError(val reason: String) : AppError
    data class SecurityError(val reason: String) : AppError
    data class UnknownError(val throwable: Throwable) : AppError
}

/**
 * کلاس وضعیت نتیجه عملیات‌های همگام و ناهمگام
 */
sealed interface Result<out T> {
    data object Loading : Result<Nothing>
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> = when (this) {
    is Result.Loading -> Result.Loading
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Error) action(error)
    return this
}

/**
 * اجرای امن بلوک‌های suspend و تبدیل استثناها به Result.Error
 */
suspend inline fun <T> safeSuspendCall(
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: IllegalArgumentException) {
    Result.Error(AppError.ValidationError(field = "input", messageResOrText = e.message ?: "ورودی نامعتبر است"))
} catch (e: Exception) {
    Result.Error(AppError.DatabaseError(causeMessage = e.localizedMessage ?: "خطای غیرمنتظره رخ داد"))
}

/**
 * تبدیل Flow معمولی به Flow<Result<T>> همراه با وضعیت Loading و مدیریت خطا
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { e ->
        emit(Result.Error(AppError.UnknownError(e)))
    }
