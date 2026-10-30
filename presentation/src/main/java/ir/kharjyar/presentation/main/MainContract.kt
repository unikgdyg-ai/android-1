package ir.kharjyar.presentation.main

import ir.kharjyar.core.money.Money
import ir.kharjyar.core.preferences.AppPreferencesState
import ir.kharjyar.domain.model.CalendarType
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.ThemeMode
import ir.kharjyar.presentation.onboarding.FirstAccountSetupData

/**
 * قرارداد Unidirectional Data Flow (UDF) برای پوسته اصلی و وضعیت سراسری برنامه
 */
data class MainUiState(
    val isLoading: Boolean = true,
    val preferences: AppPreferencesState = AppPreferencesState(),
    val totalBalance: Money = Money.fromDisplayValue(48_500_000L, CurrencyUnit.TOMAN),
    val monthlyIncome: Money = Money.fromDisplayValue(62_000_000L, CurrencyUnit.TOMAN),
    val monthlyExpense: Money = Money.fromDisplayValue(13_500_000L, CurrencyUnit.TOMAN),
    val isQuickTransactionSheetOpen: Boolean = false
)

sealed interface MainUiEvent {
    data object ToggleWorkspace : MainUiEvent
    data object TogglePrivacyMode : MainUiEvent
    data class SelectWorkspace(val workspaceId: String) : MainUiEvent
    data class CreateBusinessWorkspace(val name: String) : MainUiEvent
    data class ChangeThemeMode(val mode: ThemeMode) : MainUiEvent
    data class ChangeCurrency(val currency: CurrencyUnit) : MainUiEvent
    data class ChangeCalendar(val calendarType: CalendarType) : MainUiEvent
    data class CompleteOnboarding(val firstAccount: FirstAccountSetupData) : MainUiEvent
    data class SetQuickTransactionSheetOpen(val open: Boolean) : MainUiEvent
}

sealed interface MainUiEffect {
    data class ShowUndoSnackbar(val message: String, val deletedId: String) : MainUiEffect
    data class ShowMessage(val message: String) : MainUiEffect
}
