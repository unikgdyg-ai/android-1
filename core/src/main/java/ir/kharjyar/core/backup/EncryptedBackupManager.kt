package ir.kharjyar.core.backup

import android.util.Base64
import ir.kharjyar.core.common.AppError
import ir.kharjyar.core.common.Result
import ir.kharjyar.domain.model.Account
import ir.kharjyar.domain.model.BusinessCheck
import ir.kharjyar.domain.model.BusinessInvoice
import ir.kharjyar.domain.model.Category
import ir.kharjyar.domain.model.DebtOrReceivable
import ir.kharjyar.domain.model.FinancialTransaction
import ir.kharjyar.domain.model.SavingsGoal
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.zip.CRC32
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class KharjYarBackupPayload(
    val schemaVersion: Int = 2,
    val createdAtUtcMillis: Long,
    val checksumCrc32: Long,
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<FinancialTransaction>,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val debts: List<DebtOrReceivable> = emptyList(),
    val checks: List<BusinessCheck> = emptyList(),
    val invoices: List<BusinessInvoice> = emptyList()
)

@Singleton
class EncryptedBackupManager @Inject constructor() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun exportToJson(
        payloadWithoutChecksum: KharjYarBackupPayload,
        password: String? = null
    ): Result<String> {
        return try {
            val crc = computePayloadChecksum(payloadWithoutChecksum)
            val verifiedPayload = payloadWithoutChecksum.copy(checksumCrc32 = crc)
            val rawJson = json.encodeToString(verifiedPayload)

            if (password.isNullOrBlank()) {
                Result.Success(rawJson)
            } else {
                Result.Success(encryptAesGcm(rawJson, password))
            }
        } catch (e: Exception) {
            Result.Error(AppError.BackupRestoreError("خطا در تولید بکاپ: ${e.localizedMessage}"))
        }
    }

    fun validateAndParseBackup(
        backupContent: String,
        password: String? = null
    ): Result<KharjYarBackupPayload> {
        return try {
            val rawJson = if (backupContent.trimStart().startsWith("{")) {
                backupContent
            } else {
                if (password.isNullOrBlank()) {
                    return Result.Error(AppError.BackupRestoreError("این فایل بکاپ رمزنگاری شده است. لطفاً رمز عبور را وارد کنید."))
                }
                decryptAesGcm(backupContent, password)
            }

            val payload = json.decodeFromString<KharjYarBackupPayload>(rawJson)
            if (payload.schemaVersion !in 1..2) {
                return Result.Error(AppError.BackupRestoreError("نسخه ساختار فایل بکاپ پشتیبانی نمی‌شود."))
            }

            val expectedCrc = computePayloadChecksum(payload)
            if (payload.checksumCrc32 != expectedCrc) {
                return Result.Error(AppError.BackupRestoreError("فایل بکاپ آسیب دیده است (عدم تطابق Checksum)."))
            }

            Result.Success(payload)
        } catch (e: Exception) {
            Result.Error(AppError.BackupRestoreError("خطا در اعتبارسنجی یا رمزگشایی فایل بکاپ."))
        }
    }

    fun exportTransactionsToExcelCsv(transactions: List<FinancialTransaction>): String {
        val sb = StringBuilder("\uFEFF")
        sb.appendLine("شناسه,نوع تراکنش,مبلغ (ریال),مالیات (ریال),حساب مبدا,حساب مقصد,تاریخ (UTC),روش پرداخت,تگ‌ها,توضیحات")
        transactions.filter { !it.isDeleted }.forEach { tx ->
            val safeNote = tx.note.replace(',', ' ').replace('\n', ' ')
            val safeTags = tx.tags.joinToString("|")
            sb.appendLine(
                "${tx.id},${tx.type.name},${tx.amountMinorUnits},${tx.taxMinorUnits},${tx.accountId},${tx.targetAccountId.orEmpty()},${tx.timestampUtcMillis},${tx.paymentMethod.titleFa},$safeTags,$safeNote"
            )
        }
        return sb.toString()
    }

    private fun computePayloadChecksum(payload: KharjYarBackupPayload): Long {
        val normalized = "${payload.schemaVersion}|${payload.accounts.size}|${payload.transactions.size}|" +
            payload.transactions.sumOf { it.amountMinorUnits }
        val crc = CRC32()
        crc.update(normalized.toByteArray(Charsets.UTF_8))
        return crc.value
    }

    private fun encryptAesGcm(plainText: String, password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val secretKey = deriveAesKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = salt + iv + cipherBytes
        return "KHARJYAR_ENC_V1:" + Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptAesGcm(encryptedPayload: String, password: String): String {
        val base64Part = encryptedPayload.removePrefix("KHARJYAR_ENC_V1:").trim()
        val combined = Base64.decode(base64Part, Base64.NO_WRAP)
        val salt = combined.copyOfRange(0, 16)
        val iv = combined.copyOfRange(16, 28)
        val cipherBytes = combined.copyOfRange(28, combined.size)

        val secretKey = deriveAesKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    }

    private fun deriveAesKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65_536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
