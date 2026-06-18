package com.example.finalprojectinnobridge.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemChatReceiveBinding
import com.example.finalprojectinnobridge.databinding.ItemChatSendBinding
import com.example.finalprojectinnobridge.models.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRoomAdapter(
    private val myId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SEND = 1
        private const val VIEW_TYPE_RECEIVE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == myId) VIEW_TYPE_SEND else VIEW_TYPE_RECEIVE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SEND) {
            val binding = ItemChatSendBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SendViewHolder(binding)
        } else {
            val binding = ItemChatReceiveBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceiveViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        when (holder) {
            is SendViewHolder -> holder.bind(message, time)
            is ReceiveViewHolder -> holder.bind(message, time)
        }
    }

    inner class SendViewHolder(
        private val binding: ItemChatSendBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = time
        }
    }

    inner class ReceiveViewHolder(
        private val binding: ItemChatReceiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = time
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // 🌟 KUNCI FIX ANTI-CRASH: Membandingkan timestamp unik kiriman pesan agar aman dari NullPointerException
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message && oldItem.senderId == newItem.senderId
        }
    }
}