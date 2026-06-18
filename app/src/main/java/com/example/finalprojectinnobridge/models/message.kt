package com.example.finalprojectinnobridge.models

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)