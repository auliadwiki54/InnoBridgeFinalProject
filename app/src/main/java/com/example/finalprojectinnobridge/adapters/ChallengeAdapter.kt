package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            // Mengisi data dasar ke komponen XML item_challenge
            tvKategori.text = item.kategori
            tvJudul.text = item.judul
            tvDeskripsi.text = item.deskripsi
            tvReward.text = item.reward
            tvDeadline.text = item.deadline

            // Tambahkan pengisian data berikut jika Anda memiliki field pendukung di model data Anda:
            // tvCompanyName.text = item.namaPerusahaan
            // tvParticipants.text = "${item.jumlahPartisipan} Partisipan"

            // Aksi klik langsung pada seluruh area kartu
            root.setOnClickListener { onItemClick(item) }

            // Atau jika ingin kliknya khusus pada teks "Detail Tantangan ->"
            tvDetailLink.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Challenge>) {
        list = newList
        notifyDataSetChanged()
    }
}