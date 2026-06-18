package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.models.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageRepository {

    private val db = FirebaseDatabase.getInstance().reference.child("messages")
    private val listeners = mutableMapOf<String, ValueEventListener>()

    // Buat chatRoomId unik dan konsisten dari dua userId
    private fun chatRoomId(userA: String, userB: String): String {
        return if (userA < userB) "${userA}_${userB}" else "${userB}_${userA}"
    }

    fun sendMessage(message: Message, callback: (Boolean, String?) -> Unit) {
        val roomId = chatRoomId(message.senderId, message.receiverId)
        val ref = db.child(roomId).push()
        val msgWithId = message.copy(messageId = ref.key ?: "")
        ref.setValue(msgWithId)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.message) }
    }

    fun getMessages(
        senderId: String,
        receiverId: String,
        callback: (List<Message>?, String?) -> Unit
    ) {
        // Jika receiverId kosong → ambil semua chat yang melibatkan senderId (untuk inbox)
        if (receiverId.isEmpty()) {
            // Gunakan listener sekali saja dengan addListenerForSingleValueEvent
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allMessages = mutableListOf<Message>()
                    for (room in snapshot.children) {
                        for (msg in room.children) {
                            val m = msg.getValue(Message::class.java) ?: continue
                            if (m.senderId == senderId || m.receiverId == senderId) {
                                allMessages.add(m)
                            }
                        }
                    }
                    callback(allMessages.sortedBy { it.timestamp }, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.message)
                }
            })
        } else {
            // Chat room spesifik antara dua user
            val roomId = chatRoomId(senderId, receiverId)
            db.child(roomId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull {
                        it.getValue(Message::class.java)
                    }.sortedBy { it.timestamp }
                    callback(messages, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.message)
                }
            })
        }
    }

    fun removeListener(senderId: String, receiverId: String) {
        val key = "${senderId}_${receiverId}"
        listeners[key]?.let {
            db.removeEventListener(it)
            listeners.remove(key)
        }
    }

    fun clearAllListeners() {
        listeners.forEach { (_, listener) ->
            db.removeEventListener(listener)
        }
        listeners.clear()
    }
}