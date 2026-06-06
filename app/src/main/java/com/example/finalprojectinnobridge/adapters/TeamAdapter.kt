package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemTeamBinding
import com.example.finalprojectinnobridge.models.User

class TeamAdapter(
    private var list: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTeamBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvMemberName.text = item.nama
            tvMemberRole.text = item.role
            // Use Glide or Coil to load image if profilePicture is a URL
            
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<User>) {
        list = newList
        notifyDataSetChanged()
    }
}