package com.example.finalprojectinnobridge.repositories

import com.example.finalprojectinnobridge.models.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageRepository {

    private val db = FirebaseDatabase.getInstance().reference.child("messages")
    private val listeners = mutableMapOf<String, ValueEventListener>()

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
        val key = if (receiverId.isEmpty()) "inbox_$senderId" else "room_${chatRoomId(senderId, receiverId)}"
        
        // Remove existing listener for this key if any
        listeners[key]?.let { listener ->
            if (key.startsWith("room_")) {
                val roomId = key.removePrefix("room_")
                db.child(roomId).removeEventListener(listener)
            } else {
                db.removeEventListener(listener)
            }
        }
        listeners.remove(key)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (receiverId.isEmpty()) {
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
                } else {
                    val messages = snapshot.children.mapNotNull {
                        it.getValue(Message::class.java)
                    }.sortedBy { it.timestamp }
                    callback(messages, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        }

        // Use persistent listener for both inbox and chat room (real-time updates)
        if (receiverId.isEmpty()) {
            db.addValueEventListener(listener)
            listeners[key] = listener
        } else {
            db.child(chatRoomId(senderId, receiverId)).addValueEventListener(listener)
            listeners[key] = listener
        }
    }

    fun clearAllListeners() {
        listeners.forEach { (path, listener) ->
            if (path.startsWith("room_")) {
                val roomId = path.removePrefix("room_")
                db.child(roomId).removeEventListener(listener)
            } else {
                db.removeEventListener(listener)
            }
        }
        listeners.clear()
    }
}