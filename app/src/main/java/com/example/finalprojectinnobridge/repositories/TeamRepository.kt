package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Team
import com.example.finalprojectinnobridge.utils.Constants

class TeamRepository {
    private val db = FirebaseManager.getInstance().db

    fun createTeam(team: Team, callback: (Boolean, String?) -> Unit) {
        val docRef = db.collection(Constants.TEAMS_COLLECTION).document()
        val newTeam = team.copy(teamId = docRef.id)
        docRef.set(newTeam)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun getAllTeams(callback: (List<Team>, String?) -> Unit) {
        db.collection(Constants.TEAMS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val teams = result.toObjects(Team::class.java)
                callback(teams, null)
            }
            .addOnFailureListener {
                callback(emptyList(), it.message)
            }
    }
}