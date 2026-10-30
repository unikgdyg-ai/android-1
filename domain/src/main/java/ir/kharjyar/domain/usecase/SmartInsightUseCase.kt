package ir.kharjyar.domain.usecase

import ir.kharjyar.domain.model.BudgetStatus
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * تولید بینش‌های هوشمند فارسی بر اساس مقایسه مصرف ماه جاری با ماه قبل و روند بودجه
 */
class SmartInsightUseCase {

    fun generateInsights(budgets: List<BudgetStatus>): List<String> {
        val insights = mutableListOf<String>()

        budgets.forEach { budget ->
            if (budget.previousMonthSpentMinorUnits > 0L) {
                val diff = budget.spentMinorUnits - budget.previousMonthSpentMinorUnits
                val percentChange = ((diff.toDouble() / budget.previousMonthSpentMinorUnits.toDouble()) * 100.0).roundToInt()

                if (percentChange >= 10) {
                    insights.add("این ماه $percentChange٪ بیشتر از ماه قبل برای «${budget.categoryName}» خرج کرده‌اید.")
                } else if (percentChange <= -10) {
                    insights.add("عالی! این ماه ${abs(percentChange)}٪ کمتر از ماه قبل در دسته «${budget.categoryName}» هزینه داشته‌اید.")
                }
            }

            if (budget.isCritical100Percent) {
                insights.add("هشدار: سقف بودجه «${budget.categoryName}» در این ماه به پایان رسیده است (۱۰۰٪+).")
            } else if (budget.isWarning80Percent) {
                val pct = (budget.usageFraction * 100).roundToInt()
                insights.add("توجه: تا این لحظه $pct٪ از بودجه «${budget.categoryName}» مصرف شده است.")
            }
        }

        if (insights.isEmpty()) {
            insights.add("روند مخارج و پس‌انداز شما در این ماه متعادل و در محدوده امن بودجه قرار دارد.")
        }
        return insights
    }
}
