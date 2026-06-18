package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.ItemChallengeBinding
import com.example.finalprojectinnobridge.models.Challenge

class ChallengeAdapter(
    private var list: List<Challenge>,
    private val onItemClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvKategori.text = item.kategori
            tvJudul.text = item.judul
            tvDeskripsi.text = item.deskripsi
            tvReward.text = item.reward
            tvDeadline.text = item.deadline
            tvCompanyName.text = item.perusahaanName.ifEmpty { "Perusahaan Mitra" }

            // Load Image using Glide
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.color.secondary_blue)
                    .into(ivCompanyLogo)
            } else {
                ivCompanyLogo.setImageResource(R.drawable.ic_innovation)
            }

            root.setOnClickListener { onItemClick(item) }
            tvDetailLink.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Challenge>) {
        list = newList
        notifyDataSetChanged()
    }
}