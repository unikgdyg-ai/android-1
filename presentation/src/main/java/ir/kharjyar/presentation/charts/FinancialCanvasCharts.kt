package ir.kharjyar.presentation.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CurrencyUnit
import kotlin.math.atan2

data class CategoryChartSlice(
    val categoryName: String,
    val amountMinorUnits: Long,
    val color: Color
)

@Composable
fun InteractiveDonutChart(
    slices: List<CategoryChartSlice>,
    currency: CurrencyUnit,
    modifier: Modifier = Modifier
) {
    val sweepProgress = remember { Animatable(0f) }
    var selectedSliceIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(slices) {
        sweepProgress.snapTo(0f)
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val totalAmount = slices.sumOf { it.amountMinorUnits }.coerceAtLeast(1L)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(slices) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (touchAngle < 0f) touchAngle += 360f
                            val normalizedAngle = (touchAngle + 90f) % 360f

                            var runningAngle = 0f
                            slices.forEachIndexed { index, slice ->
                                val sweep = (slice.amountMinorUnits.toFloat() / totalAmount) * 360f
                                if (normalizedAngle in runningAngle..(runningAngle + sweep)) {
                                    selectedSliceIndex = if (selectedSliceIndex == index) -1 else index
                                    return@detectTapGestures
                                }
                                runningAngle += sweep
                            }
                        }
                    }
            ) {
                var startAngle = -90f
                val strokeWidth = 34.dp.toPx()

                slices.forEachIndexed { index, slice ->
                    val fullSweep = (slice.amountMinorUnits.toFloat() / totalAmount) * 360f
                    val animatedSweep = fullSweep * sweepProgress.value
                    val isSelected = index == selectedSliceIndex

                    drawArc(
                        color = slice.color,
                        startAngle = startAngle + 1.5f,
                        sweepAngle = (animatedSweep - 3f).coerceAtLeast(1f),
                        useCenter = false,
                        style = Stroke(
                            width = if (isSelected) strokeWidth * 1.18f else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += animatedSweep
                }
            }

            val activeSlice = slices.getOrNull(selectedSliceIndex)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = activeSlice?.categoryName ?: "مجموع هزینه‌ها",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = MoneyFormatter.format(
                        money = Money(activeSlice?.amountMinorUnits ?: totalAmount),
                        currency = currency
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AnimatedNetWorthLineChart(
    pointsMinorUnits: List<Long>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(pointsMinorUnits) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    if (pointsMinorUnits.size < 2) return

    val minVal = pointsMinorUnits.minOrNull() ?: 0L
    val maxVal = (pointsMinorUnits.maxOrNull() ?: 1L).coerceAtLeast(minVal + 1L)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        val stepX = size.width / (pointsMinorUnits.size - 1)
        val range = (maxVal - minVal).toFloat()

        val coordinates = pointsMinorUnits.mapIndexed { idx, value ->
            val x = idx * stepX
            val normalizedY = 1f - ((value - minVal).toFloat() / range)
            val y = normalizedY * (size.height * 0.85f) + (size.height * 0.08f)
            Offset(x, y)
        }

        val linePath = Path()
        val fillPath = Path()
        val visibleCount = (coordinates.size * progress.value).toInt().coerceAtLeast(2)

        coordinates.take(visibleCount).forEachIndexed { index, pt ->
            if (index == 0) {
                linePath.moveTo(pt.x, pt.y)
                fillPath.moveTo(pt.x, size.height)
                fillPath.lineTo(pt.x, pt.y)
            } else {
                linePath.lineTo(pt.x, pt.y)
                fillPath.lineTo(pt.x, pt.y)
            }
        }
        val lastPt = coordinates[visibleCount - 1]
        fillPath.lineTo(lastPt.x, size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.32f), Color.Transparent)
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun JalaliMonthlyExpenseHeatmap(
    dailySpendMinorUnits: Map<Int, Long>,
    daysInMonth: Int = 30,
    baseColor: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    val maxDailySpend = (dailySpendMinorUnits.values.maxOrNull() ?: 1L).coerceAtLeast(1L)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val rows = (1..daysInMonth).chunked(7)
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekDays.forEach { day ->
                    val spend = dailySpendMinorUnits[day] ?: 0L
                    val intensity = (spend.toFloat() / maxDailySpend.toFloat()).coerceIn(0.08f, 1f)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (spend == 0L) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        } else {
                            baseColor.copy(alpha = intensity)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = MoneyFormatter.toPersianDigits(day.toString()),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                repeat(7 - weekDays.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
