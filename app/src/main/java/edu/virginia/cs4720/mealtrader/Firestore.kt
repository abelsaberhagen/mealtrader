package edu.virginia.cs4720.mealtrader

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

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
}