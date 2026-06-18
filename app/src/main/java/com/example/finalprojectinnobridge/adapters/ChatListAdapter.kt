package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemChatInboxBinding
import com.example.finalprojectinnobridge.models.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private var chatList: List<Message>,
    private val currentUserId: String,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: ItemChatInboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val isMe = message.senderId == currentUserId
            val partnerId = if (isMe) message.receiverId else message.senderId
            val partnerName = if (isMe) message.receiverName else message.senderName

            binding.tvPartnerName.text = if (partnerName.isNullOrEmpty()) "User ($partnerId)" else partnerName
            binding.tvLastMessage.text = message.message

            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(message.timestamp))

            binding.root.setOnClickListener {
                onItemClick(partnerId, partnerName ?: "")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatInboxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

    fun updateData(newList: List<Message>) {
        this.chatList = newList
        notifyDataSetChanged()
    }
}