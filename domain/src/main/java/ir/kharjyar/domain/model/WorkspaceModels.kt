package ir.kharjyar.domain.model

import kotlinx.serialization.Serializable

/**
 * نوع فضای کاری: شخصی یا کسب‌وکار
 * هر فضا دارای دسته‌بندی‌ها، حساب‌ها، بودجه‌ها و تم رنگی کاملاً مستقل است.
 */
@Serializable
enum class WorkspaceType {
    PERSONAL,
    BUSINESS
}

/**
 * پروفایل فضای کاری (امکان تعریف یک فضای شخصی و چندین کسب‌وکار مجزا)
 */
@Serializable
data class WorkspaceProfile(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val defaultCurrency: CurrencyUnit = CurrencyUnit.TOMAN,
    val taxRateBasisPoints: Int = 1000, // پیش‌فرض ۱۰٪ (۱۰۰۰ واحد پایه از ۱۰۰۰۰) برای کسب‌وکار
    val isDefault: Boolean = false
)

/**
 * واحد پول نمایش و ثبت
 * توجه: ذخیره‌سازی داخلی همیشه به کوچک‌ترین واحد (ریال برای ایران، سنت برای دلار) به صورت Long انجام می‌شود.
 */
@Serializable
enum class CurrencyUnit(
    val code: String,
    val symbolFa: String,
    val symbolEn: String,
    val minorUnitMultiplier: Long
) {
    TOMAN(code = "IRT", symbolFa = "تومان", symbolEn = "Toman", minorUnitMultiplier = 10L),
    RIAL(code = "IRR", symbolFa = "ریال", symbolEn = "Rial", minorUnitMultiplier = 1L),
    USD(code = "USD", symbolFa = "دلار", symbolEn = "$", minorUnitMultiplier = 100L)
}

/**
 * نوع تقویم انتخابی کاربر (شمسی یا میلادی)
 */
@Serializable
enum class CalendarType {
    JALALI,
    GREGORIAN
}

/**
 * حالت پوسته رنگی برنامه
 */
@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * نوع حساب مالی برای ساخت اولین حساب در Onboarding و مدیریت حساب‌ها
 */
@Serializable
enum class AccountType(val titleFa: String, val titleEn: String) {
    BANK("حساب بانکی", "Bank Account"),
    CARD("کارت بانکی", "Debit Card"),
    CASH("وجه نقد", "Cash"),
    WALLET("کیف پول دیجیتال", "Digital Wallet"),
    SAVINGS("حساب پس‌انداز", "Savings Account")
}
