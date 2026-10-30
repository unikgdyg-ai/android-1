package ir.kharjyar.core

import com.google.common.truth.Truth.assertThat
import ir.kharjyar.core.date.JalaliDateConverter
import ir.kharjyar.core.money.ExpressionEvaluator
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CurrencyUnit
import org.junit.Test

class StageOneCoreUnitTest {

    @Test
    fun `money split into 3 parts preserves exact minor units without losing 1 rial`() {
        val total = Money(1_000_000L) // ۱,۰۰۰,۰۰۰ ریال
        val parts = total.splitIntoParts(3)

        assertThat(parts).hasSize(3)
        assertThat(parts[0].minorUnits).isEqualTo(333_334L)
        assertThat(parts[1].minorUnits).isEqualTo(333_333L)
        assertThat(parts[2].minorUnits).isEqualTo(333_333L)
        assertThat(parts.sumOf { it.minorUnits }).isEqualTo(total.minorUnits)
    }

    @Test
    fun `expression evaluator computes complex Persian digit expressions accurately`() {
        val result = ExpressionEvaluator.evaluateToLong("۱۵۰٬۰۰۰ + ۲۵٬۰۰۰ × ۲")
        assertThat(result).isEqualTo(200_000L)
    }

    @Test
    fun `money formatter hides amounts in privacy mode and formats Persian digits properly`() {
        val money = Money.fromDisplayValue(1_250_000L, CurrencyUnit.TOMAN)
        val normalFormatted = MoneyFormatter.format(money, CurrencyUnit.TOMAN, usePersianDigits = true, privacyMode = false)
        val privateFormatted = MoneyFormatter.format(money, CurrencyUnit.TOMAN, usePersianDigits = true, privacyMode = true)

        assertThat(normalFormatted).isEqualTo("۱٬۲۵۰٬۰۰۰ تومان")
        assertThat(privateFormatted).isEqualTo("•••••• تومان")
    }

    @Test
    fun `jalali date converter converts roundtrip accurately`() {
        val gregorian = JalaliDateConverter.jalaliToGregorian(1405, 7, 27)
        val jalaliBack = JalaliDateConverter.gregorianToJalali(
            gregorian.year,
            gregorian.monthValue,
            gregorian.dayOfMonth
        )

        assertThat(jalaliBack.year).isEqualTo(1405)
        assertThat(jalaliBack.month).isEqualTo(7)
        assertThat(jalaliBack.day).isEqualTo(27)
    }
}
