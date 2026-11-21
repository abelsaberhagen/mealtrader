package edu.virginia.cs4720.mealtrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.virginia.cs4720.mealtrader.ui.theme.MealtraderTheme

class MainActivity : ComponentActivity() {
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            MealtraderTheme(darkTheme = dark) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val user by AuthRepository.currentUser.collectAsStateWithLifecycle()
                    val current = user
                    Box(Modifier.padding(innerPadding)) {
                        if (current == null) LoginScreen() else ProfileGate(current.uid)
                    }
                }
            }
        }
    }
}