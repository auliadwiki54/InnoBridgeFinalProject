package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemChatReceiveBinding
import com.example.finalprojectinnobridge.databinding.ItemChatReceiveWithAvatarBinding
import com.example.finalprojectinnobridge.databinding.ItemChatSendBinding
import com.example.finalprojectinnobridge.databinding.ItemChatSendWithAvatarBinding
import com.example.finalprojectinnobridge.models.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRoomAdapter(
    private val myId: String,
    private val showAvatars: Boolean = true
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SEND = 1
        private const val VIEW_TYPE_RECEIVE = 2
        private const val VIEW_TYPE_SEND_WITH_AVATAR = 3
        private const val VIEW_TYPE_RECEIVE_WITH_AVATAR = 4
    }

    override fun getItemViewType(position: Int): Int {
        val isSender = getItem(position).senderId == myId
        return if (showAvatars) {
            if (isSender) VIEW_TYPE_SEND_WITH_AVATAR else VIEW_TYPE_RECEIVE_WITH_AVATAR
        } else {
            if (isSender) VIEW_TYPE_SEND else VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEND -> {
                val binding = ItemChatSendBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SendViewHolder(binding)
            }
            VIEW_TYPE_RECEIVE -> {
                val binding = ItemChatReceiveBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceiveViewHolder(binding)
            }
            VIEW_TYPE_SEND_WITH_AVATAR -> {
                val binding = ItemChatSendWithAvatarBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SendViewHolderWithAvatar(binding)
            }
            else -> {
                val binding = ItemChatReceiveWithAvatarBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceiveViewHolderWithAvatar(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        when (holder) {
            is SendViewHolder -> holder.bind(message, time)
            is ReceiveViewHolder -> holder.bind(message, time)
            is SendViewHolderWithAvatar -> holder.bind(message, time)
            is ReceiveViewHolderWithAvatar -> holder.bind(message, time)
        }
    }

    inner class SendViewHolder(
        private val binding: ItemChatSendBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            binding.tvMessage.text = message.message ?: ""
            binding.tvTime.text = time
        }
    }

    inner class ReceiveViewHolder(
        private val binding: ItemChatReceiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            binding.tvMessage.text = message.message ?: ""
            binding.tvTime.text = time
        }
    }

    // --- View holders dengan avatar (gaya WhatsApp) ---

    inner class SendViewHolderWithAvatar(
        private val binding: ItemChatSendWithAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            val name = (message.senderName ?: "").ifEmpty { "Saya" }
            // Nama di atas bubble
            binding.tvSenderName.text = name
            // Inisial avatar (huruf pertama nama)
            binding.tvSenderAvatar.text = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else "?"
            binding.tvMessage.text = message.message ?: ""
            binding.tvTime.text = time
        }
    }

    inner class ReceiveViewHolderWithAvatar(
        private val binding: ItemChatReceiveWithAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, time: String) {
            val name = (message.senderName ?: "").ifEmpty { "?" }
            // Nama di atas bubble
            binding.tvReceiverName.text = name
            // Inisial avatar (huruf pertama nama)
            binding.tvAvatarInitial.text = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else "?"
            binding.tvMessage.text = message.message ?: ""
            binding.tvTime.text = time
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // 🌟 Membandingkan timestamp unik agar aman dari NullPointerException pada properti lain
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // 🌟 Bandingkan field penting yang non-null untuk menghindari bug equals() crash pada field model yang null di DB
            return (oldItem.message ?: "") == (newItem.message ?: "") && 
                   (oldItem.senderId ?: "") == (newItem.senderId ?: "")
        }
    }
}
