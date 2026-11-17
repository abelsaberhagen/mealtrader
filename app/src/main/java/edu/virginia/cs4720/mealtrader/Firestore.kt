package edu.virginia.cs4720.mealtrader

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val computingId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val year: String = "",
    val availableToDonate: Boolean = false
)

val AppUser.displayName: String
    get() = "$firstName $lastName".trim().ifBlank { computingId }

object UserRepository {
    private val db = Firebase.firestore
    private val users = db.collection("users")

    suspend fun ensureUserDoc(user: FirebaseUser) {
        val ref = users.document(user.uid)
        if (ref.get().await().exists()) return
        val email = user.email ?: ""
        val data = hashMapOf(
            "uid" to user.uid,
            "email" to email,
            "computingId" to email.substringBefore("@"),
            "availableToDonate" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        ref.set(data).await()
    }

    suspend fun saveProfile(user: FirebaseUser, firstName: String, lastName: String, year: String) {
        val email = user.email ?: ""
        users.document(user.uid).set(
            mapOf(
                "uid" to user.uid,
                "email" to email,
                "computingId" to email.substringBefore("@"),
                "firstName" to firstName,
                "lastName" to lastName,
                "year" to year
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun getUser(uid: String): AppUser? =
        users.document(uid).get().await().toObject(AppUser::class.java)

    fun listenToUser(
        uid: String,
        onChange: (AppUser?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        users.document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                onError(err)
                return@addSnapshotListener
            }
            onChange(snap?.toObject(AppUser::class.java))
        }

    fun listenToOtherUsers(
        myUid: String,
        onChange: (List<AppUser>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        users.orderBy("computingId").addSnapshotListener { snap, err ->
            if (err != null) {
                onError(err)
                return@addSnapshotListener
            }
            if (snap != null) {
                onChange(snap.toObjects(AppUser::class.java).filter { it.uid != myUid })
            }
        }
}