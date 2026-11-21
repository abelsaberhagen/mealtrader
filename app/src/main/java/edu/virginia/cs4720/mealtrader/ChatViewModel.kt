package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DRAFT_KEY = "draft"

data class ChatUiState(
    val otherName: String = "",
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val otherUid: String = checkNotNull(savedStateHandle["uid"])
    val myUid: String = AuthRepository.currentUser.value?.uid ?: ""

    private val _uiState = MutableStateFlow(
        ChatUiState(draft = savedStateHandle[DRAFT_KEY] ?: "")
    )
    val uiState = _uiState.asStateFlow()

    private val registration: ListenerRegistration = ChatRepository.listenToMessages(
        chatId = ChatRepository.chatIdFor(myUid, otherUid),
        onChange = { list -> _uiState.update { it.copy(messages = list) } },
        onError = { _uiState.update { it.copy(error = "Couldn't load messages") } }
    )

    init {
        viewModelScope.launch {
            val other = runCatching { UserRepository.getUser(otherUid) }.getOrNull()
            _uiState.update { it.copy(otherName = other?.displayName ?: "Chat") }
        }
    }

    fun onDraftChange(value: String) {
        savedStateHandle[DRAFT_KEY] = value
        _uiState.update { it.copy(draft = value) }
    }

    fun send() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            try {
                ChatRepository.sendMessage(myUid, otherUid, text)
                savedStateHandle[DRAFT_KEY] = ""
                _uiState.update { it.copy(draft = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Message failed to send") }
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }

    override fun onCleared() {
        registration.remove()
    }
}