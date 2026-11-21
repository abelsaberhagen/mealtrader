package edu.virginia.cs4720.mealtrader

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val EDIT_FIRST_KEY = "edit_first"
private const val EDIT_LAST_KEY = "edit_last"
private const val EDIT_YEAR_KEY = "edit_year"
private const val EDIT_AVAILABLE_KEY = "edit_available"

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val year: String = "",
    val availableToDonate: Boolean = false,
    val savedUser: AppUser? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val myUid = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = runCatching { UserRepository.getUser(myUid) }.getOrNull()
            _uiState.update {
                it.copy(
                    firstName = savedStateHandle[EDIT_FIRST_KEY] ?: user?.firstName ?: "",
                    lastName = savedStateHandle[EDIT_LAST_KEY] ?: user?.lastName ?: "",
                    year = savedStateHandle[EDIT_YEAR_KEY] ?: user?.year ?: "",
                    availableToDonate = savedStateHandle[EDIT_AVAILABLE_KEY] ?: user?.availableToDonate ?: false,
                    savedUser = user,
                    isLoading = false,
                    error = if (user == null) "Couldn't load your profile" else null
                )
            }
        }
    }

    fun onFirstNameChange(v: String) {
        savedStateHandle[EDIT_FIRST_KEY] = v
        _uiState.update { it.copy(firstName = v, error = null, saved = false) }
    }

    fun onLastNameChange(v: String) {
        savedStateHandle[EDIT_LAST_KEY] = v
        _uiState.update { it.copy(lastName = v, error = null, saved = false) }
    }

    fun onYearChange(v: String) {
        savedStateHandle[EDIT_YEAR_KEY] = v
        _uiState.update { it.copy(year = v, error = null, saved = false) }
    }

    fun onAvailableChange(v: Boolean) {
        savedStateHandle[EDIT_AVAILABLE_KEY] = v
        _uiState.update { it.copy(availableToDonate = v, saved = false) }
    }

    fun save() {
        val s = _uiState.value
        if (s.firstName.isBlank() || s.lastName.isBlank() || s.year.isBlank()) {
            _uiState.update { it.copy(error = "Fill in every field") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val first = s.firstName.trim()
                val last = s.lastName.trim()
                UserRepository.updateProfile(
                    uid = myUid,
                    firstName = first,
                    lastName = last,
                    year = s.year,
                    availableToDonate = s.availableToDonate
                )
                savedStateHandle.remove<String>(EDIT_FIRST_KEY)
                savedStateHandle.remove<String>(EDIT_LAST_KEY)
                savedStateHandle.remove<String>(EDIT_YEAR_KEY)
                savedStateHandle.remove<Boolean>(EDIT_AVAILABLE_KEY)
                _uiState.update {
                    it.copy(
                        saved = true,
                        savedUser = it.savedUser?.copy(
                            firstName = first,
                            lastName = last,
                            year = s.year,
                            availableToDonate = s.availableToDonate
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't save. Try again.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

private fun shareAvailability(context: Context, user: AppUser) {
    val details = listOf(user.computingId, user.year).filter { it.isNotBlank() }.joinToString(", ")
    val text = "I have meal swipes to give on Meal Trader! Message me there: ${user.displayName} ($details)"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Meal swipes available")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share availability"))
}

@Composable
fun EditProfileScreen(
    onBack: (() -> Unit)? = null,
    vm: EditProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val canShare = state.savedUser?.availableToDonate == true

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Profile", onBack)
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

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = { state.savedUser?.let { shareAvailability(context, it) } },
            enabled = canShare,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text("Share my availability")
        }
        if (!canShare) {
            Text(
                "Turn on Available to donate and save to share",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { vm.save() },
            enabled = !state.isSaving && !state.saved,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(
                when {
                    state.isSaving -> "Saving..."
                    state.saved -> "Saved ✓"
                    else -> "Save"
                }
            )
        }
    }
}