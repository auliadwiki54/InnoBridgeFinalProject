package com.example.finalprojectinnobridge.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.repositories.MessageRepository

class MessageViewModel : ViewModel() {

    private val repository = MessageRepository()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _sendStatus = MutableLiveData<Pair<Boolean, String?>>()
    val sendStatus: LiveData<Pair<Boolean, String?>> = _sendStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendMessage(message: Message, callback: (Boolean, String?) -> Unit) {
        repository.sendMessage(message) { success, err ->
            _sendStatus.postValue(Pair(success, err))
            callback(success, err)
        }
    }

    // Dipakai untuk inbox (ambil semua chat user)
    // Dipakai untuk chat room (antara dua user spesifik)
    fun fetchMessages(senderId: String, receiverId: String) {
        _isLoading.value = true
        repository.getMessages(senderId, receiverId) { list, err ->
            _isLoading.postValue(false)
            if (err == null) {
                _messages.postValue(list)
            } else {
                _error.postValue(err)
            }
        }
    }
}