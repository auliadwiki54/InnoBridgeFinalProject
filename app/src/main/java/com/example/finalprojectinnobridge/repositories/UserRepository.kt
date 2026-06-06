package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.User
import com.example.finalprojectinnobridge.utils.Constants

class UserRepository {
    private val auth = FirebaseManager.getInstance().auth
    private val db = FirebaseManager.getInstance().db

    fun getUserData(uid: String, callback: (User?, String?) -> Unit) {
        db.collection(Constants.USERS_COLLECTION).document(uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user, null)
            }
            .addOnFailureListener {
                callback(null, it.message)
            }
    }

    fun saveUserData(user: User, callback: (Boolean, String?) -> Unit) {
        db.collection(Constants.USERS_COLLECTION).document(user.uid)
            .set(user)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }
}