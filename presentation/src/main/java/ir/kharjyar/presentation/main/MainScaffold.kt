package ir.kharjyar.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kharjyar.core.designsystem.components.WorkspaceHeaderBar
import ir.kharjyar.presentation.navigation.BottomNavTab
import ir.kharjyar.presentation.navigation.FloatingBottomNavBar
import ir.kharjyar.presentation.navigation.KharjYarNavHost
import ir.kharjyar.presentation.navigation.Screen
import kotlinx.coroutines.flow.Flow

/**
 * اسکلت اصلی برنامه شامل:
 * - نوار بالای سوییچ فضای کاری (شخصی / کسب‌وکار) و دکمه Privacy Mode
 * - دکمه شناور ثبت تراکنش (FAB با انیمیشن فنری و تبدیل به فرم ثبت تراکنش)
 * - نوار ناوبری پایینی شناور ۵ بخشی (FloatingBottomNavBar)
 * - مدیریت Snackbar سراسری با پشتیبانی از Undo
 */
@Composable
fun MainScaffold(
    uiState: MainUiState,
    effectFlow: Flow<MainUiEffect>,
    onEvent: (MainUiEvent) -> Unit,
    initialShortcutAction: String? = null
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isMainTabRoute = remember(currentRoute) {
        BottomNavTab.entries.any { it.route == currentRoute }
    }

    // هندل کردن میانبر اپ (App Shortcut برای «ثبت هزینه جدید»)
    LaunchedEffect(initialShortcutAction) {
        if (initialShortcutAction == "ir.kharjyar.action.NEW_EXPENSE") {
            navController.navigate(Screen.AddEditTransaction.createRoute(initialType = "EXPENSE"))
        }
    }

    // هندل کردن رخدادهای یک‌باره (Snackbar و Undo)
    LaunchedEffect(effectFlow) {
        effectFlow.collect { effect ->
            when (effect) {
                is MainUiEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is MainUiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "بازگردانی",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        // در مرحله ۲ و ۳ به UseCase بازیابی از سطل زباله متصل می‌شود
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isMainTabRoute,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(Screen.AddEditTransaction.createRoute())
                    },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "ثبت تراکنش جدید") },
                    text = { Text("ثبت تراکنش") },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 78.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isMainTabRoute) {
                    WorkspaceHeaderBar(
                        activeWorkspace = uiState.preferences.activeWorkspace,
                        privacyMode = uiState.preferences.privacyModeEnabled,
                        onToggleWorkspaceType = { onEvent(MainUiEvent.ToggleWorkspace) },
                        onTogglePrivacyMode = { onEvent(MainUiEvent.TogglePrivacyMode) }
                    )
                }

                KharjYarNavHost(
                    navController = navController,
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isMainTabRoute) {
                FloatingBottomNavBar(
                    currentRoute = currentRoute,
                    isRtl = uiState.preferences.isRtlLanguage,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
