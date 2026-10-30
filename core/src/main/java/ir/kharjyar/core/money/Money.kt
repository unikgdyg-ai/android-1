package ir.kharjyar.core.money

import ir.kharjyar.domain.model.CurrencyUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * نمایش دقیق مبالغ پولی بر حسب کوچک‌ترین واحد (ریال / سنت) به صورت Long.
 * هیچ‌گاه از Float یا Double برای ذخیره‌سازی یا محاسبه پول استفاده نمی‌شود.
 */
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    val isZero: Boolean get() = minorUnits == 0L
    val isPositive: Boolean get() = minorUnits > 0L
    val isNegative: Boolean get() = minorUnits < 0L

    operator fun plus(other: Money): Money =
        Money(Math.addExact(this.minorUnits, other.minorUnits))

    operator fun minus(other: Money): Money =
        Money(Math.subtractExact(this.minorUnits, other.minorUnits))

    operator fun unaryMinus(): Money =
        Money(Math.negateExact(this.minorUnits))

    operator fun times(factor: Long): Money =
        Money(Math.multiplyExact(this.minorUnits, factor))

    fun absValue(): Money = Money(abs(minorUnits))

    override fun compareTo(other: Money): Int =
        this.minorUnits.compareTo(other.minorUnits)

    /**
     * محاسبه دقیق درصد (مثلاً مالیات یا پیشرفت بودجه) با BigDecimal و RoundingMode.HALF_UP
     * @param basisPoints درصد ضرب در ۱۰۰ (مثلاً ۹٪ = 900، ۱۸.۵٪ = 1850)
     */
    fun percentageByBasisPoints(basisPoints: Int): Money {
        if (basisPoints == 0 || minorUnits == 0L) return ZERO
        val result = BigDecimal.valueOf(minorUnits)
            .multiply(BigDecimal.valueOf(basisPoints.toLong()))
            .divide(BigDecimal.valueOf(10_000L), 0, RoundingMode.HALF_UP)
        return Money(result.longValueExact())
    }

    /**
     * تقسیم دقیق یک مبلغ به چند بخش مساوی بدون گم شدن حتی ۱ ریال (باقیمانده به سهم اول اضافه می‌شود)
     */
    fun splitIntoParts(parts: Int): List<Money> {
        require(parts > 0) { "تعداد بخش‌های تقسیم تراکنش باید بزرگ‌تر از صفر باشد." }
        val baseShare = minorUnits / parts
        val remainder = minorUnits % parts
        return List(parts) { index ->
            if (index == 0) Money(baseShare + remainder) else Money(baseShare)
        }
    }

    /**
     * تبدیل مقدار نمایشی کاربر (مثلاً تومان) به واحد پایه دیتابیس (ریال)
     */
    companion object {
        val ZERO = Money(0L)

        fun fromDisplayValue(displayAmount: Long, currency: CurrencyUnit): Money {
            return Money(Math.multiplyExact(displayAmount, currency.minorUnitMultiplier))
        }
    }
}

/**
 * فرمت‌کننده هوشمند مبالغ مالی با پشتیبانی از جداکننده هزارگان، اعداد فارسی/انگلیسی و Privacy Mode
 */
object MoneyFormatter {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    private val threadLocalFormatter = ThreadLocal.withInitial {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
            decimalSeparator = '٫'
        }
        DecimalFormat("#,###", symbols)
    }

    /**
     * فرمت کامل مبلغ همراه با واحد پول، علامت منفی و پشتیبانی از حالت مخفی‌سازی (Privacy Mode)
     */
    fun format(
        money: Money,
        currency: CurrencyUnit = CurrencyUnit.TOMAN,
        usePersianDigits: Boolean = true,
        privacyMode: Boolean = false,
        showCurrencySymbol: Boolean = true,
        isPersianLocale: Boolean = true
    ): String {
        if (privacyMode) {
            val unitLabel = if (showCurrencySymbol) {
                " " + if (isPersianLocale) currency.symbolFa else currency.symbolEn
            } else ""
            return "••••••$unitLabel"
        }

        val displayValue = money.minorUnits / currency.minorUnitMultiplier
        val isNegative = displayValue < 0
        val absValue = abs(displayValue)

        val rawFormatted = if (currency == CurrencyUnit.USD) {
            val cents = abs(money.minorUnits % 100L)
            "${threadLocalFormatter.get().format(absValue)}.${cents.toString().padStart(2, '0')}"
        } else {
            threadLocalFormatter.get().format(absValue)
        }

        val localizedNumber = if (usePersianDigits) {
            toPersianDigits(rawFormatted)
        } else {
            rawFormatted.replace('٬', ',')
        }

        val signPrefix = if (isNegative) "-" else ""
        val symbol = if (!showCurrencySymbol) {
            ""
        } else if (isPersianLocale) {
            " ${currency.symbolFa}"
        } else {
            " ${currency.symbolEn}"
        }

        return "$signPrefix$localizedNumber$symbol"
    }

    /**
     * تبدیل اعداد انگلیسی موجود در رشته به معادل فارسی
     */
    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * نرمال‌سازی اعداد فارسی و عربی ورودی کاربر به انگلیسی جهت پردازش ریاضی
     */
    fun normalizeDigitsToEnglish(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                '٬', ',', ' ' -> { /* حذف جداکننده‌های هزارگان */ }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
