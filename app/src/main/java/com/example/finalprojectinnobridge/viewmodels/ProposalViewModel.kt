package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.repositories.ProposalRepository

class ProposalViewModel : ViewModel() {
    private val repository = ProposalRepository()

    private val _proposals = MutableLiveData<List<Proposal>>()
    val proposals: LiveData<List<Proposal>> = _proposals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchProposalsByUser(userId: String) {
        _isLoading.value = true
        repository.getProposalsByUser(userId) { list, err ->
            _isLoading.value = false
            if (err == null) {
                _proposals.value = list
            } else {
                _error.value = err
            }
        }
    }

    fun fetchProposalsByChallenge(challengeId: String) {
        _isLoading.value = true
        repository.getProposalsByChallenge(challengeId) { list, err ->
            _isLoading.value = false
            if (err == null) {
                _proposals.value = list
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
}