package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.models.Team
import com.example.finalprojectinnobridge.repositories.TeamRepository

class TeamViewModel : ViewModel() {
    private val repository = TeamRepository()

    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchTeams() {
        _isLoading.value = true
        repository.getAllTeams { list, _ ->
            _isLoading.value = false
            _teams.value = list
        }
    }

    fun createTeam(team: Team, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        repository.createTeam(team) { success, err ->
            _isLoading.value = false
            callback(success, err)
        }
    }
}