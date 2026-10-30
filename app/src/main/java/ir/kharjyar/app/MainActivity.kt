package ir.kharjyar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.kharjyar.core.designsystem.theme.KharjYarTheme
import ir.kharjyar.presentation.main.MainScaffold
import ir.kharjyar.presentation.main.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val shortcutAction = intent?.action

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val prefs = uiState.preferences

            KharjYarTheme(
                workspaceType = prefs.activeWorkspace.type,
                themeMode = prefs.themeMode,
                useDynamicColor = prefs.useDynamicColor,
                isRtl = prefs.isRtlLanguage
            ) {
                MainScaffold(
                    uiState = uiState,
                    effectFlow = mainViewModel.effect,
                    onEvent = mainViewModel::onEvent,
                    initialShortcutAction = shortcutAction
                )
            }
        }
    }
}
