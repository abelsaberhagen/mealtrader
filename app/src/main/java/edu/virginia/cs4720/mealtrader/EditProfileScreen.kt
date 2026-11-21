package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val year: String = "",
    val availableToDonate: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel : ViewModel() {
    private val myUid = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = runCatching { UserRepository.getUser(myUid) }.getOrNull()
            _uiState.update {
                it.copy(
                    firstName = user?.firstName ?: "",
                    lastName = user?.lastName ?: "",
                    year = user?.year ?: "",
                    availableToDonate = user?.availableToDonate ?: false,
                    isLoading = false,
                    error = if (user == null) "Couldn't load your profile" else null
                )
            }
        }
    }

    fun onFirstNameChange(v: String) = _uiState.update { it.copy(firstName = v, error = null) }
    fun onLastNameChange(v: String) = _uiState.update { it.copy(lastName = v, error = null) }
    fun onYearChange(v: String) = _uiState.update { it.copy(year = v, error = null) }
    fun onAvailableChange(v: Boolean) = _uiState.update { it.copy(availableToDonate = v) }

    fun save() {
        val s = _uiState.value
        if (s.firstName.isBlank() || s.lastName.isBlank() || s.year.isBlank()) {
            _uiState.update { it.copy(error = "Fill in every field") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                UserRepository.updateProfile(
                    uid = myUid,
                    firstName = s.firstName.trim(),
                    lastName = s.lastName.trim(),
                    year = s.year,
                    availableToDonate = s.availableToDonate
                )
                _uiState.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't save. Try again.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    vm: EditProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Edit Profile", onBack)
        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

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

        Text("Year", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            YEAR_OPTIONS.forEach { option ->
                FilterChip(
                    selected = state.year == option,
                    onClick = { vm.onYearChange(option) },
                    label = { Text(option.removeSuffix(" Year")) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Available to donate", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Show others you have swipes to give",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.availableToDonate,
                onCheckedChange = { vm.onAvailableChange(it) }
            )
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
            Text(if (state.isSaving) "Saving..." else "Save")
        }
    }
}