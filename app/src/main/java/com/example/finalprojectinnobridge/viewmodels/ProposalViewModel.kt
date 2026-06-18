package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.repositories.ProposalRepository
import com.google.firebase.firestore.ListenerRegistration

class ProposalViewModel : ViewModel() {
    private val repository = ProposalRepository()
    private var allProposalsListener: ListenerRegistration? = null
    private var userProposalsListener: ListenerRegistration? = null

    private val _proposals = MutableLiveData<List<Proposal>>()
    val proposals: LiveData<List<Proposal>> = _proposals

    private val _userProposals = MutableLiveData<List<Proposal>>()
    val userProposals: LiveData<List<Proposal>> = _userProposals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun listenToAllProposals() {
        allProposalsListener?.remove()
        allProposalsListener = repository.listenAllProposals { list, err ->
            if (err == null) {
                _proposals.value = list
            } else {
                _error.value = err
            }
        }
    }

    fun listenToUserProposals(userId: String) {
        userProposalsListener?.remove()
        userProposalsListener = repository.listenProposalsByUser(userId) { list, err ->
            if (err == null) {
                _userProposals.value = list
            } else {
                _error.value = err
            }
        }
    }

    fun fetchProposalsByUser(userId: String) {
        _isLoading.value = true
        repository.getProposalsByUser(userId) { list, err ->
            _isLoading.value = false
            if (err == null) {
                _userProposals.value = list
            } else {
                _error.value = err
            }
        }
    }

    fun submitProposal(proposal: Proposal, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        repository.submitProposal(proposal) { success, err ->
            _isLoading.value = false
            callback(success, err)
        }
    }

    fun updateStatus(proposalId: String, status: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        repository.updateProposalStatus(proposalId, status) { success, err ->
            _isLoading.value = false
            callback(success, err)
        }
    }

    fun updateScoreAndEvaluation(proposalId: String, score: Int, evaluasi: String, status: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        val updates = mapOf(
            "score" to score,
            "evaluasi" to evaluasi,
            "status" to status
        )
        com.example.finalprojectinnobridge.firebase.FirebaseManager.getInstance().db
            .collection(com.example.finalprojectinnobridge.utils.Constants.PROPOSALS_COLLECTION)
            .document(proposalId)
            .update(updates)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    override fun onCleared() {
        super.onCleared()
        allProposalsListener?.remove()
        userProposalsListener?.remove()
    }
}