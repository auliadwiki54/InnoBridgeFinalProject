package com.example.finalprojectinnobridge.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.ItemMessageBinding
import com.example.finalprojectinnobridge.models.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private var list: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvMessage.text = item.message
            
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(item.timestamp))

            val params = cvMessageContainer.layoutParams as LinearLayout.LayoutParams
            if (item.senderId == currentUserId) {
                params.gravity = Gravity.END
                cvMessageContainer.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.primary))
                tvMessage.setTextColor(ContextCompat.getColor(root.context, R.color.white))
            } else {
                params.gravity = Gravity.START
                cvMessageContainer.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.white))
                tvMessage.setTextColor(ContextCompat.getColor(root.context, R.color.text_dark))
            }
            cvMessageContainer.layoutParams = params
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Message>) {
        list = newList
        notifyDataSetChanged()
    }
}