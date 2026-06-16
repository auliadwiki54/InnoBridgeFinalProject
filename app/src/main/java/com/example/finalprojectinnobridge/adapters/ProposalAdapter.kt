package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemProposalBinding
import com.example.finalprojectinnobridge.models.Proposal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProposalAdapter(
    private var list: List<Proposal>,
    private val onItemClick: (Proposal) -> Unit
) : RecyclerView.Adapter<ProposalAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProposalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProposalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvJudulSolusi.text = item.judul
            tvStatusBadge.text = item.status
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvTanggal.text = sdf.format(Date(item.tanggal))
            
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Proposal>) {
        list = newList
        notifyDataSetChanged()
    }
}
