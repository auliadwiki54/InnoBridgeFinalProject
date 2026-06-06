package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.utils.Constants
import com.google.firebase.firestore.Query

class MessageRepository {
    private val db = FirebaseManager.getInstance().db

    fun sendMessage(message: Message, callback: (Boolean, String?) -> Unit) {
        val docRef = db.collection(Constants.MESSAGES_COLLECTION).document()
        val newMessage = message.copy(messageId = docRef.id)
        docRef.set(newMessage)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun getMessages(senderId: String, receiverId: String, callback: (List<Message>, String?) -> Unit) {
        db.collection(Constants.MESSAGES_COLLECTION)
            .whereIn("senderId", listOf(senderId, receiverId))
            .whereIn("receiverId", listOf(senderId, receiverId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    callback(emptyList(), error.message)
                    return@addSnapshotListener
                }
                val messages = value?.toObjects(Message::class.java) ?: emptyList()
                // Filter manually because whereIn doesn't support complex logical OR in this way easily without composite indexes
                val filtered = messages.filter { 
                    (it.senderId == senderId && it.receiverId == receiverId) || 
                    (it.senderId == receiverId && it.receiverId == senderId)
                }
                callback(filtered, null)
            }
    }
}