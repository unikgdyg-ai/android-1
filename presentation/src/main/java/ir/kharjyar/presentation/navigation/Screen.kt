package ir.kharjyar.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * مسیرهای ناوبری برنامه «خرجیار»
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")

    // ۵ تب اصلی Bottom Navigation شناور
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object Reports : Screen("reports")
    data object Budget : Screen("budget")
    data object More : Screen("more")

    // صفحات فرعی و ماژول‌های تخصصی
    data object AddEditTransaction : Screen("transaction_form?txId={txId}&initialType={initialType}") {
        fun createRoute(txId: String? = null, initialType: String = "EXPENSE"): String =
            "transaction_form?txId=${txId.orEmpty()}&initialType=$initialType"
    }
    data object Accounts : Screen("accounts")
    data object Categories : Screen("categories")
    data object SavingsGoals : Screen("savings_goals")
    data object DebtsAndReceivables : Screen("debts_receivables")
    data object BusinessHub : Screen("business_hub")
    data object TrashBin : Screen("trash_bin")
    data object Settings : Screen("settings")
}

/**
 * آیتم‌های ۵گانه Bottom Navigation شناور
 */
enum class BottomNavTab(
    val route: String,
    val titleFa: String,
    val titleEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Screen.Home.route,
        titleFa = "خانه",
        titleEn = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TRANSACTIONS(
        route = Screen.Transactions.route,
        titleFa = "تراکنش‌ها",
        titleEn = "Transactions",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong
    ),
    REPORTS(
        route = Screen.Reports.route,
        titleFa = "گزارش‌ها",
        titleEn = "Reports",
        selectedIcon = Icons.Filled.PieChart,
        unselectedIcon = Icons.Outlined.PieChart
    ),
    BUDGET(
        route = Screen.Budget.route,
        titleFa = "بودجه",
        titleEn = "Budget",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    ),
    MORE(
        route = Screen.More.route,
        titleFa = "بیشتر",
        titleEn = "More",
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView
    )
}
