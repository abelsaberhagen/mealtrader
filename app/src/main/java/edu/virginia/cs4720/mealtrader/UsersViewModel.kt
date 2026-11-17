package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UsersUiState(
    val users: List<AppUser> = emptyList(),
    val previews: Map<String, ChatPreview> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class UsersViewModel : ViewModel() {
    val myUid: String = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState = _uiState.asStateFlow()

    private var rawUsers: List<AppUser> = emptyList()
    private var previews: Map<String, ChatPreview> = emptyMap()

    private val usersRegistration: ListenerRegistration =
        UserRepository.listenToOtherUsers(
            myUid = myUid,
            onChange = { list ->
                rawUsers = list
                publish()
            },
            onError = {
                _uiState.update { it.copy(isLoading = false, error = "Couldn't load users") }
            }
        )

    private val chatsRegistration: ListenerRegistration =
        ChatRepository.listenToMyChats(
            myUid = myUid,
            onChange = { map ->
                previews = map
                publish()
            },
            onError = { }
        )

    private fun publish() {
        val sorted = rawUsers.sortedWith(
            compareByDescending<AppUser> { previews[it.uid]?.updatedAt ?: 0L }
                .thenBy { it.displayName.lowercase() }
        )
        _uiState.update {
            it.copy(users = sorted, previews = previews, isLoading = false, error = null)
        }
    }

    override fun onCleared() {
        usersRegistration.remove()
        chatsRegistration.remove()
    }
}