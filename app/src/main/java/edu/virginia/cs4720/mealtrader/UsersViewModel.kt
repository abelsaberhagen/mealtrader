package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UsersUiState(
    val users: List<AppUser> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class UsersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState = _uiState.asStateFlow()

    private val registration: ListenerRegistration? =
        AuthRepository.currentUser.value?.uid?.let { uid ->
            UserRepository.listenToOtherUsers(
                myUid = uid,
                onChange = { list ->
                    _uiState.update { it.copy(users = list, isLoading = false, error = null) }
                },
                onError = {
                    _uiState.update { it.copy(isLoading = false, error = "Couldn't load users") }
                }
            )
        }

    override fun onCleared() {
        registration?.remove()
    }
}