package ir.kharjyar.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kharjyar.core.preferences.AppPreferencesDataStore
import ir.kharjyar.domain.model.WorkspaceType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataStore: AppPreferencesDataStore
) : ViewModel() {

    private val isQuickSheetOpen = MutableStateFlow(false)
    private val initialAccountBalance = MutableStateFlow<ir.kharjyar.core.money.Money?>(null)

    private val _effect = Channel<MainUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<MainUiState> = combine(
        preferencesDataStore.preferencesFlow,
        isQuickSheetOpen,
        initialAccountBalance
    ) { prefs, sheetOpen, customBalance ->
        MainUiState(
            isLoading = false,
            preferences = prefs,
            totalBalance = customBalance ?: ir.kharjyar.core.money.Money.fromDisplayValue(
                if (prefs.activeWorkspace.type == WorkspaceType.BUSINESS) 185_400_000L else 48_500_000L,
                prefs.currencyUnit
            ),
            monthlyIncome = ir.kharjyar.core.money.Money.fromDisplayValue(
                if (prefs.activeWorkspace.type == WorkspaceType.BUSINESS) 240_000_000L else 62_000_000L,
                prefs.currencyUnit
            ),
            monthlyExpense = ir.kharjyar.core.money.Money.fromDisplayValue(
                if (prefs.activeWorkspace.type == WorkspaceType.BUSINESS) 54_600_000L else 13_500_000L,
                prefs.currencyUnit
            ),
            isQuickTransactionSheetOpen = sheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun onEvent(event: MainUiEvent) {
        viewModelScope.launch {
            when (event) {
                MainUiEvent.ToggleWorkspace -> {
                    val current = uiState.value.preferences
                    val targetType = if (current.activeWorkspace.type == WorkspaceType.PERSONAL) {
                        WorkspaceType.BUSINESS
                    } else {
                        WorkspaceType.PERSONAL
                    }
                    val targetWorkspace = current.workspaces.firstOrNull { it.type == targetType }
                    if (targetWorkspace != null) {
                        preferencesDataStore.switchWorkspace(targetWorkspace.id)
                        _effect.send(MainUiEffect.ShowMessage("فضای کاری به «${targetWorkspace.name}» تغییر کرد"))
                    }
                }
                MainUiEvent.TogglePrivacyMode -> {
                    preferencesDataStore.togglePrivacyMode()
                }
                is MainUiEvent.SelectWorkspace -> {
                    preferencesDataStore.switchWorkspace(event.workspaceId)
                }
                is MainUiEvent.CreateBusinessWorkspace -> {
                    val created = preferencesDataStore.addBusinessWorkspace(event.name)
                    _effect.send(MainUiEffect.ShowMessage("کسب‌وکار «${created.name}» ساخته و فعال شد"))
                }
                is MainUiEvent.ChangeThemeMode -> {
                    preferencesDataStore.setThemeMode(event.mode)
                }
                is MainUiEvent.ChangeCurrency -> {
                    preferencesDataStore.setCurrencyUnit(event.currency)
                }
                is MainUiEvent.ChangeCalendar -> {
                    preferencesDataStore.setCalendarType(event.calendarType)
                }
                is MainUiEvent.CompleteOnboarding -> {
                    initialAccountBalance.value = event.firstAccount.initialBalance
                    preferencesDataStore.setCurrencyUnit(event.firstAccount.selectedCurrency)
                    preferencesDataStore.completeOnboarding()
                    _effect.send(
                        MainUiEffect.ShowMessage(
                            "حساب «${event.firstAccount.accountName}» با موفقیت ساخته شد"
                        )
                    )
                }
                is MainUiEvent.SetQuickTransactionSheetOpen -> {
                    isQuickSheetOpen.value = event.open
                }
            }
        }
    }
}
