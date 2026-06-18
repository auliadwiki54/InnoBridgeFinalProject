package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.utils.Constants

class ChallengeRepository {
    private val db = FirebaseManager.getInstance().db

    fun getAllChallenges(callback: (List<Challenge>, String?) -> Unit) {
        db.collection(Constants.CHALLENGES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val challenges = result.toObjects(Challenge::class.java)
                callback(challenges, null)
            }
            .addOnFailureListener {
                callback(emptyList(), it.message)
            }
    }

    fun addChallenge(challenge: Challenge, callback: (Boolean, String?) -> Unit) {
        val docRef = db.collection(Constants.CHALLENGES_COLLECTION).document()
        val newChallenge = challenge.copy(challengeId = docRef.id)
        docRef.set(newChallenge)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    // 🌟 KUNCI FIX: Fungsi Update murni menimpa dokumen lama berdasarkan ChallengeId aslinya
    fun updateChallenge(challenge: Challenge, callback: (Boolean, String?) -> Unit) {
        if (challenge.challengeId.isBlank()) {
            callback(false, "ID Tantangan tidak valid atau kosong")
            return
        }

        // Membuka dokumen spesifik berdasarkan id yang mau diedit, lalu menimpanya dengan data baru
        db.collection(Constants.CHALLENGES_COLLECTION)
            .document(challenge.challengeId)
            .set(challenge) // .set() akan otomatis mengganti (overwrite) data lama di Firestore
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun deleteChallenge(challengeId: String, callback: (Boolean, String?) -> Unit) {
        db.collection(Constants.CHALLENGES_COLLECTION).document(challengeId)
            .delete()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }
}