package ir.kharjyar.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.designsystem.theme.KharjYarThemeExtensions
import ir.kharjyar.domain.model.BudgetStatus
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.usecase.SmartInsightUseCase
import ir.kharjyar.presentation.charts.AnimatedNetWorthLineChart
import ir.kharjyar.presentation.charts.CategoryChartSlice
import ir.kharjyar.presentation.charts.InteractiveDonutChart
import ir.kharjyar.presentation.charts.JalaliMonthlyExpenseHeatmap

@Composable
fun ReportsTabScreen(
    currency: CurrencyUnit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by rememberSaveable { mutableStateOf("ماه جاری") }
    val financialColors = KharjYarThemeExtensions.financialColors
    val palette = financialColors.chartPalette

    val donutSlices = remember(palette) {
        listOf(
            CategoryChartSlice("مسکن و اجاره", 85_000_000L, palette[0]),
            CategoryChartSlice("خوراک و روزمره", 42_000_000L, palette[1]),
            CategoryChartSlice("حمل‌ونقل و خودرو", 18_500_000L, palette[2]),
            CategoryChartSlice("سلامت و درمان", 12_000_000L, palette[3]),
            CategoryChartSlice("تفریح و فرهنگ", 9_500_000L, palette[4])
        )
    }

    val netWorthTrend = remember {
        listOf(310_000_000L, 345_000_000L, 330_000_000L, 395_000_000L, 440_000_000L, 485_000_000L)
    }

    val heatmapData = remember {
        (1..30).associateWith { day ->
            when {
                day % 7 == 0 -> 14_000_000L
                day % 3 == 0 -> 6_500_000L
                day % 2 == 0 -> 2_200_000L
                else -> 0L
            }
        }
    }

    val insights = remember {
        SmartInsightUseCase().generateInsights(
            listOf(
                BudgetStatus(
                    id = "b_food",
                    workspaceId = "ws",
                    categoryId = "cat_food",
                    categoryName = "خوراک و روزمره",
                    year = 1405,
                    month = 7,
                    limitMinorUnits = 50_000_000L,
                    spentMinorUnits = 42_000_000L,
                    previousMonthSpentMinorUnits = 35_500_000L
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("امروز", "هفته", "ماه جاری", "سالانه").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "بینش هوشمند مالی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    insights.forEach { text ->
                        Text(text = "• $text", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "تفکیک هزینه‌ها بر اساس دسته‌بندی (روی بخش‌ها ضربه بزنید)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InteractiveDonutChart(slices = donutSlices, currency = currency)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "روند رشد دارایی خالص (۶ ماه اخیر)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedNetWorthLineChart(pointsMinorUnits = netWorthTrend)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "تقویم حرارتی هزینه‌های روزانه ماه شمسی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    JalaliMonthlyExpenseHeatmap(dailySpendMinorUnits = heatmapData)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}
