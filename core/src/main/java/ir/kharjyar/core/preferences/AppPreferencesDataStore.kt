package ir.kharjyar.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.kharjyar.domain.model.CalendarType
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.ThemeMode
import ir.kharjyar.domain.model.WorkspaceProfile
import ir.kharjyar.domain.model.WorkspaceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kharjyar_settings")

/**
 * وضعیت جامع تنظیمات برنامه که به صورت Reactive در کل اپ منتشر می‌شود
 */
data class AppPreferencesState(
    val activeWorkspaceId: String = DEFAULT_PERSONAL_WORKSPACE_ID,
    val workspaces: List<WorkspaceProfile> = defaultWorkspaces(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val calendarType: CalendarType = CalendarType.JALALI,
    val usePersianDigits: Boolean = true,
    val isRtlLanguage: Boolean = true,
    val privacyModeEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false
) {
    val activeWorkspace: WorkspaceProfile
        get() = workspaces.find { it.id == activeWorkspaceId } ?: workspaces.first()

    companion object {
        const val DEFAULT_PERSONAL_WORKSPACE_ID = "workspace_personal_default"
        const val DEFAULT_BUSINESS_WORKSPACE_ID = "workspace_business_default"

        fun defaultWorkspaces(): List<WorkspaceProfile> = listOf(
            WorkspaceProfile(
                id = DEFAULT_PERSONAL_WORKSPACE_ID,
                name = "حساب شخصی",
                type = WorkspaceType.PERSONAL,
                defaultCurrency = CurrencyUnit.TOMAN,
                isDefault = true
            ),
            WorkspaceProfile(
                id = DEFAULT_BUSINESS_WORKSPACE_ID,
                name = "کسب‌وکار من",
                type = WorkspaceType.BUSINESS,
                defaultCurrency = CurrencyUnit.TOMAN,
                isDefault = false
            )
        )
    }
}

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val ACTIVE_WORKSPACE_ID = stringPreferencesKey("active_workspace_id")
        val WORKSPACES_JSON = stringPreferencesKey("workspaces_json")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val CURRENCY_UNIT = stringPreferencesKey("currency_unit")
        val CALENDAR_TYPE = stringPreferencesKey("calendar_type")
        val USE_PERSIAN_DIGITS = booleanPreferencesKey("use_persian_digits")
        val IS_RTL_LANGUAGE = booleanPreferencesKey("is_rtl_language")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val workspaces = prefs[Keys.WORKSPACES_JSON]?.let {
                runCatching { json.decodeFromString<List<WorkspaceProfile>>(it) }.getOrNull()
            } ?: AppPreferencesState.defaultWorkspaces()

            AppPreferencesState(
                activeWorkspaceId = prefs[Keys.ACTIVE_WORKSPACE_ID]
                    ?: AppPreferencesState.DEFAULT_PERSONAL_WORKSPACE_ID,
                workspaces = workspaces,
                themeMode = prefs[Keys.THEME_MODE]?.let {
                    runCatching { ThemeMode.valueOf(it) }.getOrNull()
                } ?: ThemeMode.SYSTEM,
                useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
                currencyUnit = prefs[Keys.CURRENCY_UNIT]?.let {
                    runCatching { CurrencyUnit.valueOf(it) }.getOrNull()
                } ?: CurrencyUnit.TOMAN,
                calendarType = prefs[Keys.CALENDAR_TYPE]?.let {
                    runCatching { CalendarType.valueOf(it) }.getOrNull()
                } ?: CalendarType.JALALI,
                usePersianDigits = prefs[Keys.USE_PERSIAN_DIGITS] ?: true,
                isRtlLanguage = prefs[Keys.IS_RTL_LANGUAGE] ?: true,
                privacyModeEnabled = prefs[Keys.PRIVACY_MODE] ?: false,
                biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
            )
        }

    suspend fun switchWorkspace(workspaceId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_WORKSPACE_ID] = workspaceId
        }
    }

    suspend fun addBusinessWorkspace(name: String): WorkspaceProfile {
        val newProfile = WorkspaceProfile(
            id = "workspace_business_${System.currentTimeMillis()}",
            name = name.trim(),
            type = WorkspaceType.BUSINESS
        )
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WORKSPACES_JSON]?.let {
                runCatching { json.decodeFromString<List<WorkspaceProfile>>(it) }.getOrNull()
            } ?: AppPreferencesState.defaultWorkspaces()
            val updated = current + newProfile
            prefs[Keys.WORKSPACES_JSON] = json.encodeToString(updated)
            prefs[Keys.ACTIVE_WORKSPACE_ID] = newProfile.id
        }
        return newProfile
    }

    suspend fun togglePrivacyMode() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PRIVACY_MODE] ?: false
            prefs[Keys.PRIVACY_MODE] = !current
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setCurrencyUnit(unit: CurrencyUnit) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENCY_UNIT] = unit.name
        }
    }

    suspend fun setCalendarType(type: CalendarType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_TYPE] = type.name
        }
    }

    suspend fun setUsePersianDigits(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USE_PERSIAN_DIGITS] = enabled
        }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
