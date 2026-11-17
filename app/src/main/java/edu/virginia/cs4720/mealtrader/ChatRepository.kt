package edu.virginia.cs4720.mealtrader

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val sentAt: Long = 0L
)

data class ChatPreview(
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val updatedAt: Long = 0L
)

object ChatRepository {
    private val chats = Firebase.firestore.collection("chats")

    fun chatIdFor(a: String, b: String) = listOf(a, b).sorted().joinToString("_")

    fun listenToMessages(
        chatId: String,
        onChange: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        chats.document(chatId).collection("messages")
            .orderBy("sentAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err)
                    return@addSnapshotListener
                }
                if (snap != null) onChange(snap.toObjects(Message::class.java))
            }

    fun listenToMyChats(
        myUid: String,
        onChange: (Map<String, ChatPreview>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        chats.whereArrayContains("participants", myUid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err)
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val previews = snap.toObjects(ChatPreview::class.java)
                        .mapNotNull { chat ->
                            val other = chat.participants.firstOrNull { it != myUid }
                            other?.let { it to chat }
                        }
                        .toMap()
                    onChange(previews)
                }
            }

    suspend fun sendMessage(myUid: String, otherUid: String, text: String) {
        val chatId = chatIdFor(myUid, otherUid)
        val now = System.currentTimeMillis()
        val chatRef = chats.document(chatId)
        chatRef.set(
            mapOf(
                "participants" to listOf(myUid, otherUid).sorted(),
                "lastMessage" to text,
                "lastSenderId" to myUid,
                "updatedAt" to now
            ),
            SetOptions.merge()
        ).await()
        chatRef.collection("messages").add(
            mapOf("senderId" to myUid, "text" to text, "sentAt" to now)
        ).await()
    }
}