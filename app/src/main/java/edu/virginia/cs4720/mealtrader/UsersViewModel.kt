package edu.virginia.cs4720.mealtrader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersUiState(
    val users: List<AppUser> = emptyList(),
    val previews: Map<String, ChatPreview> = emptyMap(),
    val friendIds: Set<String> = emptySet(),
    val availableOnly: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    val myUid: String = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState = _uiState.asStateFlow()

    private var rawUsers: List<AppUser>? = null
    private var previews: Map<String, ChatPreview> = emptyMap()
    private var settings: AppSettings = AppSettings()

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

    private val friendsRegistration: ListenerRegistration =
        UserRepository.listenToFriendIds(
            myUid = myUid,
            onChange = { ids -> _uiState.update { it.copy(friendIds = ids) } },
            onError = { }
        )

    init {
        viewModelScope.launch {
            SettingsRepository(application).settings.collect {
                settings = it
                publish()
            }
        }
    }

    fun addFriend(uid: String) {
        viewModelScope.launch {
            try {
                UserRepository.addFriend(uid)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Couldn't add friend") }
            }
        }
    }

    private fun publish() {
        val users = rawUsers ?: return
        val filtered = if (settings.availableOnly) users.filter { it.availableToDonate } else users
        val sorted = when (settings.sortMode) {
            SortMode.RECENT -> filtered.sortedWith(
                compareByDescending<AppUser> { previews[it.uid]?.updatedAt ?: 0L }
                    .thenBy { it.displayName.lowercase() }
            )
            SortMode.NAME -> filtered.sortedBy { it.displayName.lowercase() }
        }
        _uiState.update {
            it.copy(
                users = sorted,
                previews = previews,
                availableOnly = settings.availableOnly,
                isLoading = false,
                error = null
            )
        }
    }

    override fun onCleared() {
        usersRegistration.remove()
        chatsRegistration.remove()
        friendsRegistration.remove()
    }
}