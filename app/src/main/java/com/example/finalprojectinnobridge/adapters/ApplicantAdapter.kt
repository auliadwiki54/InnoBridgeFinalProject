package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.databinding.ItemApplicantBinding

data class ApplicantListItem(
    val userId: String,
    val name: String,
    val university: String,
    val department: String,
    val skill: String,
    val proposalCount: Int,
    val totalScore: Int,
    val photoUrl: String = ""
)

class ApplicantAdapter(
    private var list: List<ApplicantListItem>,
    private val onChatClick: (ApplicantListItem) -> Unit
) : RecyclerView.Adapter<ApplicantAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemApplicantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApplicantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvName.text = item.name.ifEmpty { "Inovator" }
            tvUniversity.text = item.university.ifEmpty { "Universitas" }
            
            tvAcademic.text = "Program Studi: " + item.department.ifEmpty { "-" }
            tvSkills.text = "Keahlian: " + item.skill.ifEmpty { "-" }
            
            tvProposalBadge.text = "${item.proposalCount} Proposal"
            tvContributionBadge.text = "Skor: ${item.totalScore}"

            // Load profile picture or initial
            if (item.photoUrl.isNotEmpty()) {
                ivAvatar.visibility = View.VISIBLE
                tvAvatarInitial.visibility = View.GONE
                Glide.with(holder.itemView.context)
                    .load(item.photoUrl)
                    .placeholder(R.color.secondary_blue)
                    .into(ivAvatar)
            } else {
                ivAvatar.visibility = View.GONE
                tvAvatarInitial.visibility = View.VISIBLE
                val initial = if (item.name.isNotEmpty()) item.name.first().uppercaseChar().toString() else "?"
                tvAvatarInitial.text = initial
            }

            btnChat.setOnClickListener { onChatClick(item) }
            root.setOnClickListener { onChatClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ApplicantListItem>) {
        list = newList
        notifyDataSetChanged()
    }
}
