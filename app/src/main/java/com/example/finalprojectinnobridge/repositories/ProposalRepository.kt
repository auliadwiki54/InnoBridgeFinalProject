package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.utils.Constants
import com.google.firebase.firestore.ListenerRegistration

class ProposalRepository {
    private val db = FirebaseManager.getInstance().db

    fun submitProposal(proposal: Proposal, callback: (Boolean, String?) -> Unit) {
        val docRef = db.collection(Constants.PROPOSALS_COLLECTION).document()
        val newProposal = proposal.copy(proposalId = docRef.id)
        docRef.set(newProposal)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun getProposalsByUser(userId: String, callback: (List<Proposal>, String?) -> Unit) {
        db.collection(Constants.PROPOSALS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val proposals = result.toObjects(Proposal::class.java)
                callback(proposals, null)
            }
            .addOnFailureListener {
                callback(emptyList(), it.message)
            }
    }

    // New: Real-time listener for all proposals (useful for company stats)
    fun listenAllProposals(callback: (List<Proposal>, String?) -> Unit): ListenerRegistration {
        return db.collection(Constants.PROPOSALS_COLLECTION)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    callback(emptyList(), error.message)
                    return@addSnapshotListener
                }
                val proposals = value?.toObjects(Proposal::class.java) ?: emptyList()
                callback(proposals, null)
            }
    }

    // New: Real-time listener for user proposals
    fun listenProposalsByUser(userId: String, callback: (List<Proposal>, String?) -> Unit): ListenerRegistration {
        return db.collection(Constants.PROPOSALS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    callback(emptyList(), error.message)
                    return@addSnapshotListener
                }
                val proposals = value?.toObjects(Proposal::class.java) ?: emptyList()
                callback(proposals, null)
            }
    }

    fun getProposalsByChallenge(challengeId: String, callback: (List<Proposal>, String?) -> Unit) {
        db.collection(Constants.PROPOSALS_COLLECTION)
            .whereEqualTo("challengeId", challengeId)
            .get()
            .addOnSuccessListener { result ->
                val proposals = result.toObjects(Proposal::class.java)
                callback(proposals, null)
            }
            .addOnFailureListener {
                callback(emptyList(), it.message)
            }
    }

    fun updateProposalStatus(proposalId: String, status: String, callback: (Boolean, String?) -> Unit) {
        db.collection(Constants.PROPOSALS_COLLECTION).document(proposalId)
            .update("status", status)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }
}