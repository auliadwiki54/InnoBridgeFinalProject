package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.User
import com.example.finalprojectinnobridge.repositories.UserRepository

class AuthViewModel : ViewModel() {
    private val auth = FirebaseManager.getInstance().auth
    private val repository = UserRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun login(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    repository.getUserData(uid) { userData, err ->
                        _isLoading.value = false
                        if (userData != null) {
                            _user.value = userData
                            callback(true, null)
                        } else {
                            _error.value = err ?: "Gagal mengambil data user"
                            callback(false, _error.value)
                        }
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                    callback(false, _error.value)
                }
            }
    }

    fun register(name: String, email: String, pass: String, role: String, callback: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    val newUser = User(uid = uid, nama = name, email = email, role = role)
                    repository.saveUserData(newUser) { success, err ->
                        _isLoading.value = false
                        if (success) {
                            _user.value = newUser
                            callback(true, null)
                        } else {
                            _error.value = err
                            callback(false, err)
                        }
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                    callback(false, _error.value)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }
}