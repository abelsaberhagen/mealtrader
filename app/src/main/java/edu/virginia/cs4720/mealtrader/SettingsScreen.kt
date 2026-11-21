package edu.virginia.cs4720.mealtrader

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SettingsRepository(application)

    val settings = repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setAvailableOnly(v: Boolean) = viewModelScope.launch { repo.setAvailableOnly(v) }
    fun setSortMode(mode: SortMode) = viewModelScope.launch { repo.setSortMode(mode) }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Settings", onBack)
        Spacer(Modifier.height(24.dp))

        Text("Theme", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark")
                .forEach { (mode, label) ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { vm.setThemeMode(mode) },
                        label = { Text(label) }
                    )
                }
        }
        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Only show available donors", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Hide users who aren't offering swipes in Browse Users",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = settings.availableOnly, onCheckedChange = { vm.setAvailableOnly(it) })
        }
        Spacer(Modifier.height(24.dp))

        Text("Sort users by", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(SortMode.RECENT to "Recent chats", SortMode.NAME to "Name")
                .forEach { (mode, label) ->
                    FilterChip(
                        selected = settings.sortMode == mode,
                        onClick = { vm.setSortMode(mode) },
                        label = { Text(label) }
                    )
                }
        }
    }
}