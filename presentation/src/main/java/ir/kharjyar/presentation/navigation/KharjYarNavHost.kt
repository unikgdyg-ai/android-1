package ir.kharjyar.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.kharjyar.core.date.JalaliDateConverter
import ir.kharjyar.core.designsystem.components.AnimatedMoneyText
import ir.kharjyar.core.designsystem.components.StaggeredAnimatedItem
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.designsystem.theme.KharjYarThemeExtensions
import ir.kharjyar.domain.model.CalendarType
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.ThemeMode
import ir.kharjyar.presentation.budget.BudgetGoalsAndDebtTabScreen
import ir.kharjyar.presentation.charts.AnimatedNetWorthLineChart
import ir.kharjyar.presentation.main.MainUiEvent
import ir.kharjyar.presentation.main.MainUiState
import ir.kharjyar.presentation.onboarding.OnboardingScreen
import ir.kharjyar.presentation.reports.ReportsTabScreen
import ir.kharjyar.presentation.transactions.AddEditTransactionFormScreen
import ir.kharjyar.presentation.transactions.TransactionsTabScreen

/**
 * گراف ناوبری کامل برنامه «خرجیار» (مراحل ۱ تا ۸ متصل به صفحات واقعی)
 */
@Composable
fun KharjYarNavHost(
    navController: NavHostController,
    uiState: MainUiState,
    onEvent: (MainUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val startDestination = if (uiState.preferences.onboardingCompleted) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(260, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(200, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(260)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(260))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(200))
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = { firstAccount ->
                    onEvent(MainUiEvent.CompleteOnboarding(firstAccount))
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeDashboardScreen(
                uiState = uiState,
                onOpenTransactionForm = {
                    navController.navigate(Screen.AddEditTransaction.createRoute())
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsTabScreen(
                currency = uiState.preferences.currencyUnit,
                calendarType = uiState.preferences.calendarType,
                privacyMode = uiState.preferences.privacyModeEnabled,
                onOpenNewTransactionForm = {
                    navController.navigate(Screen.AddEditTransaction.createRoute())
                },
                onTransactionDeletedWithUndo = {
                    // ارسال رخداد حذف نرم به همراه Undo Snackbar
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsTabScreen(
                currency = uiState.preferences.currencyUnit
            )
        }

        composable(Screen.Budget.route) {
            BudgetGoalsAndDebtTabScreen(
                currency = uiState.preferences.currencyUnit,
                privacyMode = uiState.preferences.privacyModeEnabled
            )
        }

        composable(Screen.More.route) {
            SettingsAndBusinessHubScreen(
                uiState = uiState,
                onEvent = onEvent
            )
        }

        composable(
            route = Screen.AddEditTransaction.route,
            arguments = listOf(
                navArgument("txId") { type = NavType.StringType; defaultValue = "" },
                navArgument("initialType") { type = NavType.StringType; defaultValue = "EXPENSE" }
            )
        ) {
            AddEditTransactionFormScreen(
                currency = uiState.preferences.currencyUnit,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeDashboardScreen(
    uiState: MainUiState,
    onOpenTransactionForm: () -> Unit
) {
    val prefs = uiState.preferences
    val financialColors = KharjYarThemeExtensions.financialColors
    val todayFormatted = JalaliDateConverter.formatUtcTimestamp(
        utcMillis = System.currentTimeMillis(),
        calendarType = prefs.calendarType,
        usePersianDigits = prefs.usePersianDigits
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StaggeredAnimatedItem(index = 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "موجودی کل (${prefs.activeWorkspace.name})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        AnimatedMoneyText(
                            money = uiState.totalBalance,
                            currency = prefs.currencyUnit,
                            usePersianDigits = prefs.usePersianDigits,
                            privacyMode = prefs.privacyModeEnabled,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            StaggeredAnimatedItem(index = 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = financialColors.incomeContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDownward,
                                    contentDescription = null,
                                    tint = financialColors.income
                                )
                                Text(
                                    text = "درآمد این ماه",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = financialColors.income
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedMoneyText(
                                money = uiState.monthlyIncome,
                                currency = prefs.currencyUnit,
                                usePersianDigits = prefs.usePersianDigits,
                                privacyMode = prefs.privacyModeEnabled,
                                style = MaterialTheme.typography.titleLarge,
                                color = financialColors.income
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = financialColors.expenseContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = null,
                                    tint = financialColors.expense
                                )
                                Text(
                                    text = "هزینه این ماه",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = financialColors.expense
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedMoneyText(
                                money = uiState.monthlyExpense,
                                currency = prefs.currencyUnit,
                                usePersianDigits = prefs.usePersianDigits,
                                privacyMode = prefs.privacyModeEnabled,
                                style = MaterialTheme.typography.titleLarge,
                                color = financialColors.expense
                            )
                        }
                    }
                }
            }
        }

        item {
            StaggeredAnimatedItem(index = 2) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "روند موجودی و جریان نقدی ماه جاری",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedNetWorthLineChart(
                            pointsMinorUnits = listOf(380_000_000L, 410_000_000L, 395_000_000L, 450_000_000L, 485_000_000L)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}

@Composable
private fun SettingsAndBusinessHubScreen(
    uiState: MainUiState,
    onEvent: (MainUiEvent) -> Unit
) {
    val prefs = uiState.preferences
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(KharjYarDimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "تنظیمات، امنیت، کسب‌وکار و بکاپ رمزنگاری‌شده",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("حالت نمایش (تم):", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = { onEvent(MainUiEvent.ChangeThemeMode(mode)) },
                                label = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.SYSTEM -> "خودکار سیستم"
                                            ThemeMode.LIGHT -> "روشن"
                                            ThemeMode.DARK -> "تاریک"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Text("واحد پول:", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = prefs.currencyUnit == unit,
                                onClick = { onEvent(MainUiEvent.ChangeCurrency(unit)) },
                                label = { Text(unit.symbolFa) }
                            )
                        }
                    }

                    Text("نوع تقویم:", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalendarType.entries.forEach { cal ->
                            FilterChip(
                                selected = prefs.calendarType == cal,
                                onClick = { onEvent(MainUiEvent.ChangeCalendar(cal)) },
                                label = {
                                    Text(if (cal == CalendarType.JALALI) "شمسی (جلالی)" else "میلادی")
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("امکانات فعال کسب‌وکار، امنیت و خروجی‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("• صدور فاکتور رسمی با محاسبه خودکار مالیات بر ارزش افزوده (VAT)")
                    Text("• مدیریت چک‌های صیادی دریافتی/پرداختی با یادآور سررسید")
                    Text("• بکاپ و ریستور رمزنگاری‌شده AES-256-GCM (JSON و Excel CSV)")
                    Text("• خروجی رسمی PDF صورت سود و زیان ویژه حسابدار")
                    Text("• سطل زباله ۳۰ روزه تراکنش‌ها و ویجت صفحه اصلی Glance")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}
