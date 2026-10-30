package ir.kharjyar.core.date

import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CalendarType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * مدل تاریخ شمسی (جلالی) همراه با تبدیل دوطرفه به میلادی و UTC Timestamp
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    init {
        require(month in 1..12) { "ماه شمسی باید بین ۱ تا ۱۲ باشد." }
        require(day in 1..31) { "روز شمسی باید بین ۱ تا ۳۱ باشد." }
    }

    val monthNameFa: String
        get() = JALALI_MONTH_NAMES_FA[month - 1]

    fun format(usePersianDigits: Boolean = true, includeMonthName: Boolean = true): String {
        val raw = if (includeMonthName) {
            "$day $monthNameFa $year"
        } else {
            "%04d/%02d/%02d".format(year, month, day)
        }
        return if (usePersianDigits) MoneyFormatter.toPersianDigits(raw) else raw
    }

    companion object {
        val JALALI_MONTH_NAMES_FA = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        val PERSIAN_WEEKDAYS_FROM_SATURDAY = listOf(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
        )
    }
}

object JalaliDateConverter {

    /**
     * تبدیل تاریخ میلادی به شمسی (جلالی) بر اساس الگوریتم دقیق گاه‌شماری جلالی
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gMonth > 2) gYear + 1 else gYear
        var days = 355666 + (365 * gYear) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gDay + gDaysInMonth[gMonth - 1]

        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    /**
     * تبدیل تاریخ شمسی (جلالی) به میلادی
     */
    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): LocalDate {
        val jy = jYear + 1595
        var days = -355668 + (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + jDay +
            if (jMonth < 7) (jMonth - 1) * 31 else ((jMonth - 7) * 30) + 186

        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val salA = intArrayOf(0, 31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 1
        while (gm <= 12 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return LocalDate.of(gy, gm, gd)
    }

    /**
     * تبدیل UTC Epoch Millis به رشته تاریخ خوانا بر اساس تقویم انتخابی کاربر
     */
    fun formatUtcTimestamp(
        utcMillis: Long,
        calendarType: CalendarType = CalendarType.JALALI,
        usePersianDigits: Boolean = true,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val zonedDateTime = Instant.ofEpochMilli(utcMillis).atZone(zoneId)
        return when (calendarType) {
            CalendarType.JALALI -> {
                val jalali = gregorianToJalali(
                    zonedDateTime.year,
                    zonedDateTime.monthValue,
                    zonedDateTime.dayOfMonth
                )
                jalali.format(usePersianDigits = usePersianDigits, includeMonthName = true)
            }
            CalendarType.GREGORIAN -> {
                val raw = "%04d-%02d-%02d".format(
                    zonedDateTime.year,
                    zonedDateTime.monthValue,
                    zonedDateTime.dayOfMonth
                )
                if (usePersianDigits) MoneyFormatter.toPersianDigits(raw) else raw
            }
        }
    }

    /**
     * محاسبه اندیس روز هفته با شروع از شنبه (۰ = شنبه، ۱ = یکشنبه، ...، ۶ = جمعه)
     */
    fun dayOfWeekIndexFromSaturday(utcMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int {
        val dayOfWeek = Instant.ofEpochMilli(utcMillis).atZone(zoneId).dayOfWeek.value // 1=Mon .. 7=Sun
        return (dayOfWeek + 1) % 7
    }
}
