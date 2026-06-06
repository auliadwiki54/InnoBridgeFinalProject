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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendMessage(message: Message, callback: (Boolean, String?) -> Unit) {
        repository.sendMessage(message, callback)
    }

    fun fetchMessages(senderId: String, receiverId: String) {
        _isLoading.value = true
        repository.getMessages(senderId, receiverId) { list, err ->
            _isLoading.value = false
            if (err == null) {
                _messages.value = list
            } else {
                _error.value = err
            }
        }
    }
}