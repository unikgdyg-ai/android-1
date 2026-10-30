package ir.kharjyar.core.designsystem.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.kharjyar.domain.model.ThemeMode
import ir.kharjyar.domain.model.WorkspaceType

private val PersonalLightScheme = lightColorScheme(
    primary = PersonalPrimaryLight,
    onPrimary = PersonalOnPrimaryLight,
    primaryContainer = PersonalPrimaryContainerLight,
    onPrimaryContainer = PersonalOnPrimaryContainerLight,
    secondary = PersonalSecondaryLight,
    onSecondary = PersonalOnSecondaryLight,
    secondaryContainer = PersonalSecondaryContainerLight,
    onSecondaryContainer = PersonalOnSecondaryContainerLight,
    tertiary = PersonalTertiaryLight,
    onTertiary = PersonalOnTertiaryLight,
    tertiaryContainer = PersonalTertiaryContainerLight,
    onTertiaryContainer = PersonalOnTertiaryContainerLight,
    background = PersonalBackgroundLight,
    surface = PersonalSurfaceLight,
    surfaceVariant = PersonalSurfaceVariantLight,
    onSurface = PersonalOnSurfaceLight,
    onSurfaceVariant = PersonalOnSurfaceVariantLight
)

private val PersonalDarkScheme = darkColorScheme(
    primary = PersonalPrimaryDark,
    onPrimary = PersonalOnPrimaryDark,
    primaryContainer = PersonalPrimaryContainerDark,
    onPrimaryContainer = PersonalOnPrimaryContainerDark,
    secondary = PersonalSecondaryDark,
    onSecondary = PersonalOnSecondaryDark,
    secondaryContainer = PersonalSecondaryContainerDark,
    onSecondaryContainer = PersonalOnSecondaryContainerDark,
    tertiary = PersonalTertiaryDark,
    onTertiary = PersonalOnTertiaryDark,
    tertiaryContainer = PersonalTertiaryContainerDark,
    onTertiaryContainer = PersonalOnTertiaryContainerDark,
    background = PersonalBackgroundDark,
    surface = PersonalSurfaceDark,
    surfaceVariant = PersonalSurfaceVariantDark,
    onSurface = PersonalOnSurfaceDark,
    onSurfaceVariant = PersonalOnSurfaceVariantDark
)

private val BusinessLightScheme = lightColorScheme(
    primary = BusinessPrimaryLight,
    onPrimary = BusinessOnPrimaryLight,
    primaryContainer = BusinessPrimaryContainerLight,
    onPrimaryContainer = BusinessOnPrimaryContainerLight,
    secondary = BusinessSecondaryLight,
    onSecondary = BusinessOnSecondaryLight,
    secondaryContainer = BusinessSecondaryContainerLight,
    onSecondaryContainer = BusinessOnSecondaryContainerLight,
    tertiary = BusinessTertiaryLight,
    onTertiary = BusinessOnTertiaryLight,
    tertiaryContainer = BusinessTertiaryContainerLight,
    onTertiaryContainer = BusinessOnTertiaryContainerLight,
    background = BusinessBackgroundLight,
    surface = BusinessSurfaceLight,
    surfaceVariant = BusinessSurfaceVariantLight,
    onSurface = BusinessOnSurfaceLight,
    onSurfaceVariant = BusinessOnSurfaceVariantLight
)

private val BusinessDarkScheme = darkColorScheme(
    primary = BusinessPrimaryDark,
    onPrimary = BusinessOnPrimaryDark,
    primaryContainer = BusinessPrimaryContainerDark,
    onPrimaryContainer = BusinessOnPrimaryContainerDark,
    secondary = BusinessSecondaryDark,
    onSecondary = BusinessOnSecondaryDark,
    secondaryContainer = BusinessSecondaryContainerDark,
    onSecondaryContainer = BusinessOnSecondaryContainerDark,
    tertiary = BusinessTertiaryDark,
    onTertiary = BusinessOnTertiaryDark,
    tertiaryContainer = BusinessTertiaryContainerDark,
    onTertiaryContainer = BusinessOnTertiaryContainerDark,
    background = BusinessBackgroundDark,
    surface = BusinessSurfaceDark,
    surfaceVariant = BusinessSurfaceVariantDark,
    onSurface = BusinessOnSurfaceDark,
    onSurfaceVariant = BusinessOnSurfaceVariantDark
)

val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }
val LocalWorkspaceType = staticCompositionLocalOf { WorkspaceType.PERSONAL }

@Composable
private fun Color.animateThemeColor(): Color {
    return animateColorAsState(
        targetValue = this,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ThemeColorTransition"
    ).value
}

@Composable
private fun ColorScheme.animateAllColors(): ColorScheme = copy(
    primary = primary.animateThemeColor(),
    onPrimary = onPrimary.animateThemeColor(),
    primaryContainer = primaryContainer.animateThemeColor(),
    onPrimaryContainer = onPrimaryContainer.animateThemeColor(),
    secondary = secondary.animateThemeColor(),
    onSecondary = onSecondary.animateThemeColor(),
    secondaryContainer = secondaryContainer.animateThemeColor(),
    onSecondaryContainer = onSecondaryContainer.animateThemeColor(),
    tertiary = tertiary.animateThemeColor(),
    background = background.animateThemeColor(),
    surface = surface.animateThemeColor(),
    surfaceVariant = surfaceVariant.animateThemeColor(),
    onSurface = onSurface.animateThemeColor(),
    onSurfaceVariant = onSurfaceVariant.animateThemeColor()
)

object KharjYarThemeExtensions {
    val financialColors: FinancialColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinancialColors.current

    val workspaceType: WorkspaceType
        @Composable
        @ReadOnlyComposable
        get() = LocalWorkspaceType.current
}

/**
 * تم اصلی برنامه «خرجیار» با پشتیبانی از:
 * - تفکیک رنگی خودکار فضای «شخصی» و «کسب‌وکار» همراه با انیمیشن تغییر رنگ نرم
 * - پشتیبانی از Material You (Dynamic Color در اندروید ۱۲ به بالا)
 * - پشتیبانی کامل از جهت‌چین راست‌به‌چپ (RTL) و چپ‌به‌راست (LTR)
 */
@Composable
fun KharjYarTheme(
    workspaceType: WorkspaceType = WorkspaceType.PERSONAL,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val baseColorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        workspaceType == WorkspaceType.BUSINESS -> {
            if (darkTheme) BusinessDarkScheme else BusinessLightScheme
        }
        else -> {
            if (darkTheme) PersonalDarkScheme else PersonalLightScheme
        }
    }

    val animatedColorScheme = baseColorScheme.animateAllColors()
    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalFinancialColors provides financialColors,
        LocalWorkspaceType provides workspaceType
    ) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = KharjYarTypography,
            shapes = KharjYarShapes,
            content = content
        )
    }
}
