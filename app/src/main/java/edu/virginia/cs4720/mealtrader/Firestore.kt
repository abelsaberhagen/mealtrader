package edu.virginia.cs4720.mealtrader

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private const val TAG = "Firestore"

fun addTestUser() {
    val db = Firebase.firestore

    // Create a new user with a first and last name
    val user = hashMapOf(
        "first" to "Ada",
        "last" to "Lovelace",
        "born" to 1815,
    )

    // Add a new document with a generated ID
    db.collection("users")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
}
