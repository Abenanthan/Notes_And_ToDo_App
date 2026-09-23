package com.example.notestodo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notestodo.data.repository.AppSettings
import com.example.notestodo.data.repository.ThemeMode
import com.example.notestodo.ui.AppNavigation
import com.example.notestodo.ui.theme.NotesToDoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = (application as NotesApp).settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
                .value

            val darkTheme = when (settings.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            // Applied again whenever the choice changes, so the status and navigation bar
            // icons follow the app's own theme rather than the system setting.
            LaunchedEffect(darkTheme) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
            }

            NotesToDoTheme(darkTheme = darkTheme, dynamicColor = settings.dynamicColor) {
                AppNavigation()
            }
        }
    }
}

// The scrims AndroidX itself uses behind a translucent navigation bar on older versions.
private val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
