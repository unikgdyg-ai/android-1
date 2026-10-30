package ir.kharjyar.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// ۱. پالت فضای شخصی (Personal Workspace) — Emerald & Mint Teal
// ---------------------------------------------------------------------------
val PersonalPrimaryLight = Color(0xFF006C51)
val PersonalOnPrimaryLight = Color(0xFFFFFFFF)
val PersonalPrimaryContainerLight = Color(0xFF83F8CD)
val PersonalOnPrimaryContainerLight = Color(0xFF002116)

val PersonalSecondaryLight = Color(0xFF4C6359)
val PersonalOnSecondaryLight = Color(0xFFFFFFFF)
val PersonalSecondaryContainerLight = Color(0xFFCEE9DB)
val PersonalOnSecondaryContainerLight = Color(0xFF082018)

val PersonalTertiaryLight = Color(0xFF3E6374)
val PersonalOnTertiaryLight = Color(0xFFFFFFFF)
val PersonalTertiaryContainerLight = Color(0xFFC2E8FD)
val PersonalOnTertiaryContainerLight = Color(0xFF001F2A)

val PersonalBackgroundLight = Color(0xFFF6FBF8)
val PersonalSurfaceLight = Color(0xFFFFFFFF)
val PersonalSurfaceVariantLight = Color(0xFFDBE5DF)
val PersonalOnSurfaceLight = Color(0xFF171D1A)
val PersonalOnSurfaceVariantLight = Color(0xFF3F4944)

// حالت تاریک عمیق و چشم‌نواز فضای شخصی (Obsidian Emerald)
val PersonalPrimaryDark = Color(0xFF66DBB2)
val PersonalOnPrimaryDark = Color(0xFF003828)
val PersonalPrimaryContainerDark = Color(0xFF00513C)
val PersonalOnPrimaryContainerDark = Color(0xFF83F8CD)

val PersonalSecondaryDark = Color(0xFFB3CCC0)
val PersonalOnSecondaryDark = Color(0xFF1E352C)
val PersonalSecondaryContainerDark = Color(0xFF354B42)
val PersonalOnSecondaryContainerDark = Color(0xFFCEE9DB)

val PersonalTertiaryDark = Color(0xFFA6CCE0)
val PersonalOnTertiaryDark = Color(0xFF093544)
val PersonalTertiaryContainerDark = Color(0xFF254B5C)
val PersonalOnTertiaryContainerDark = Color(0xFFC2E8FD)

val PersonalBackgroundDark = Color(0xFF0D1412)
val PersonalSurfaceDark = Color(0xFF151E1B)
val PersonalSurfaceVariantDark = Color(0xFF23302B)
val PersonalOnSurfaceDark = Color(0xFFDEE4E0)
val PersonalOnSurfaceVariantDark = Color(0xFFBFC9C3)

// ---------------------------------------------------------------------------
// ۲. پالت فضای کسب‌وکار (Business Workspace) — Royal Indigo & Warm Gold
// ---------------------------------------------------------------------------
val BusinessPrimaryLight = Color(0xFF34469D)
val BusinessOnPrimaryLight = Color(0xFFFFFFFF)
val BusinessPrimaryContainerLight = Color(0xFFDEE1FF)
val BusinessOnPrimaryContainerLight = Color(0xFF001258)

val BusinessSecondaryLight = Color(0xFF7A5900)
val BusinessOnSecondaryLight = Color(0xFFFFFFFF)
val BusinessSecondaryContainerLight = Color(0xFFFFDEA1)
val BusinessOnSecondaryContainerLight = Color(0xFF261900)

val BusinessTertiaryLight = Color(0xFF006879)
val BusinessOnTertiaryLight = Color(0xFFFFFFFF)
val BusinessTertiaryContainerLight = Color(0xFFA9EDFF)
val BusinessOnTertiaryContainerLight = Color(0xFF001F26)

val BusinessBackgroundLight = Color(0xFFF8F9FF)
val BusinessSurfaceLight = Color(0xFFFFFFFF)
val BusinessSurfaceVariantLight = Color(0xFFE2E1EC)
val BusinessOnSurfaceLight = Color(0xFF1A1B22)
val BusinessOnSurfaceVariantLight = Color(0xFF45464F)

// حالت تاریک حرفه‌ای فضای کسب‌وکار (Midnight Indigo & Gold)
val BusinessPrimaryDark = Color(0xFFB9C3FF)
val BusinessOnPrimaryDark = Color(0xFF0F257D)
val BusinessPrimaryContainerDark = Color(0xFF293CA0)
val BusinessOnPrimaryContainerDark = Color(0xFFDEE1FF)

val BusinessSecondaryDark = Color(0xFFF6BE46)
val BusinessOnSecondaryDark = Color(0xFF402D00)
val BusinessSecondaryContainerDark = Color(0xFF5C4200)
val BusinessOnSecondaryContainerDark = Color(0xFFFFDEA1)

val BusinessTertiaryDark = Color(0xFF54D7F3)
val BusinessOnTertiaryDark = Color(0xFF003640)
val BusinessTertiaryContainerDark = Color(0xFF004E5C)
val BusinessOnTertiaryContainerDark = Color(0xFFA9EDFF)

val BusinessBackgroundDark = Color(0xFF0F111A)
val BusinessSurfaceDark = Color(0xFF171A26)
val BusinessSurfaceVariantDark = Color(0xFF25293A)
val BusinessOnSurfaceDark = Color(0xFFE3E1EC)
val BusinessOnSurfaceVariantDark = Color(0xFFC6C5D0)

// ---------------------------------------------------------------------------
// ۳. رنگ‌های معنایی مالی (Financial Semantic Colors)
// ---------------------------------------------------------------------------
data class FinancialColors(
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val transfer: Color,
    val transferContainer: Color,
    val budgetSafe: Color,
    val budgetWarning80: Color,
    val budgetCritical100: Color,
    val chartPalette: List<Color>
)

val LightFinancialColors = FinancialColors(
    income = Color(0xFF12805C),
    incomeContainer = Color(0xFFD6F5E8),
    expense = Color(0xFFD93848),
    expenseContainer = Color(0xFFFFE5E8),
    transfer = Color(0xFF1D6CE0),
    transferContainer = Color(0xFFDCEBFF),
    budgetSafe = Color(0xFF12805C),
    budgetWarning80 = Color(0xFFE68A00),
    budgetCritical100 = Color(0xFFD93848),
    chartPalette = listOf(
        Color(0xFF00897B),
        Color(0xFF3949AB),
        Color(0xFFF4511E),
        Color(0xFF8E24AA),
        Color(0xFFFB8C00),
        Color(0xFF039BE5),
        Color(0xFF43A047),
        Color(0xFFE53935)
    )
)

val DarkFinancialColors = FinancialColors(
    income = Color(0xFF4EE0A8),
    incomeContainer = Color(0xFF0F3D2D),
    expense = Color(0xFFFF808B),
    expenseContainer = Color(0xFF4C151D),
    transfer = Color(0xFF78B3FF),
    transferContainer = Color(0xFF112E57),
    budgetSafe = Color(0xFF4EE0A8),
    budgetWarning80 = Color(0xFFFFB74D),
    budgetCritical100 = Color(0xFFFF6E7A),
    chartPalette = listOf(
        Color(0xFF4DB6AC),
        Color(0xFF7986CB),
        Color(0xFFFF8A65),
        Color(0xFFBA68C8),
        Color(0xFFFFB74D),
        Color(0xFF4FC3F7),
        Color(0xFF81C784),
        Color(0xFFE57373)
    )
)
