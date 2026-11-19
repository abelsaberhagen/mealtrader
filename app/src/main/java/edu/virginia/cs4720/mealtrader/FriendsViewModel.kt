package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FriendsUiState(
    val friends: List<AppUser> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FriendsViewModel : ViewModel() {
    val myUid: String = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState = _uiState.asStateFlow()

    private var friendIds: Set<String>? = null
    private var allUsers: List<AppUser>? = null

    private val friendsRegistration: ListenerRegistration =
        UserRepository.listenToFriendIds(
            myUid = myUid,
            onChange = { ids ->
                friendIds = ids
                publish()
            },
            onError = { setError() }
        )

    private val usersRegistration: ListenerRegistration =
        UserRepository.listenToOtherUsers(
            myUid = myUid,
            onChange = { list ->
                allUsers = list
                publish()
            },
            onError = { setError() }
        )

    private fun publish() {
        val ids = friendIds ?: return   // wait until both listeners have fired once
        val users = allUsers ?: return
        val friends = users
            .filter { it.uid in ids }
            .sortedBy { it.displayName.lowercase() }
        _uiState.update { it.copy(friends = friends, isLoading = false, error = null) }
    }

    private fun setError() {
        _uiState.update { it.copy(isLoading = false, error = "Couldn't load friends") }
    }

    override fun onCleared() {
        friendsRegistration.remove()
        usersRegistration.remove()
    }
}
