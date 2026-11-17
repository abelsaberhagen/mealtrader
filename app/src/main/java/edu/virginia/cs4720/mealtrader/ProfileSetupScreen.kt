package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val YEAR_OPTIONS = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "Grad")

data class ProfileSetupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val year: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

class ProfileSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState = _uiState.asStateFlow()

    fun onFirstNameChange(v: String) = _uiState.update { it.copy(firstName = v, error = null) }
    fun onLastNameChange(v: String) = _uiState.update { it.copy(lastName = v, error = null) }
    fun onYearChange(v: String) = _uiState.update { it.copy(year = v, error = null) }

    fun save() {
        val s = _uiState.value
        val user = AuthRepository.currentUser.value ?: return
        if (s.firstName.isBlank() || s.lastName.isBlank() || s.year.isBlank()) {
            _uiState.update { it.copy(error = "Fill in every field") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                UserRepository.saveProfile(user, s.firstName.trim(), s.lastName.trim(), s.year)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't save profile. Try again.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    uid: String,
    vm: ProfileSetupViewModel = viewModel(key = "setup_$uid")
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set up your profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Other students will see your name and year")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.firstName,
            onValueChange = { vm.onFirstNameChange(it) },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.lastName,
            onValueChange = { vm.onLastNameChange(it) },
            label = { Text("Last name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Year", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            YEAR_OPTIONS.forEach { option ->
                FilterChip(
                    selected = state.year == option,
                    onClick = { vm.onYearChange(option) },
                    label = { Text(option.removeSuffix(" Year")) }
                )
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.save() },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving..." else "Continue")
        }
        TextButton(onClick = { AuthRepository.signOut() }) { Text("Sign out") }
    }
}