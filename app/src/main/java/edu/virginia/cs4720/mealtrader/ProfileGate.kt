package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ProfileState { Loading, Incomplete, Complete }

class SessionViewModel : ViewModel() {
    private val uid = AuthRepository.currentUser.value?.uid ?: ""

    private val _state = MutableStateFlow(ProfileState.Loading)
    val state = _state.asStateFlow()

    private val registration: ListenerRegistration = UserRepository.listenToUser(
        uid = uid,
        onChange = { user ->
            val complete = user != null &&
                    user.firstName.isNotBlank() &&
                    user.lastName.isNotBlank() &&
                    user.year.isNotBlank()
            _state.value = if (complete) ProfileState.Complete else ProfileState.Incomplete
        },
        onError = { _state.value = ProfileState.Incomplete }
    )

    override fun onCleared() {
        registration.remove()
    }
}

@Composable
fun ProfileGate(uid: String) {
    val vm: SessionViewModel = viewModel(key = "session_$uid")
    val state by vm.state.collectAsStateWithLifecycle()

    when (state) {
        ProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        ProfileState.Incomplete -> ProfileSetupScreen(uid)
        ProfileState.Complete -> AppNavigation()
    }
}