package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.repositories.ChallengeRepository

class ChallengeViewModel : ViewModel() {
    private val repository = ChallengeRepository()

    private val _challenges = MutableLiveData<List<Challenge>>()
    val challenges: LiveData<List<Challenge>> = _challenges

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchChallenges() {
        _isLoading.value = true
        repository.getAllChallenges { list, err ->
            _isLoading.value = false
            if (err == null) {
                _challenges.value = list
            } else {
                _error.value = err
            }
        }
    }

    fun addChallenge(challenge: Challenge, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        repository.addChallenge(challenge) { success, err ->
            _isLoading.value = false
            callback(success, err)
        }
    }

    fun deleteChallenge(challengeId: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        repository.deleteChallenge(challengeId) { success, err ->
            _isLoading.value = false
            callback(success, err)
        }
    }
}