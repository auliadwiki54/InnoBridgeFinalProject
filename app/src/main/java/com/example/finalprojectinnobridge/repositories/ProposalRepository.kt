package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.utils.Constants

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