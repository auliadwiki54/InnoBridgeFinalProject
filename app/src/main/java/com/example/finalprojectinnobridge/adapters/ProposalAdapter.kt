package com.example.finalprojectinnobridge.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectinnobridge.databinding.ItemProposalBinding
import com.example.finalprojectinnobridge.models.Proposal
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.utils.Constants
import com.example.finalprojectinnobridge.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProposalAdapter(
    private var list: List<Proposal>,
    private val onDetailClick: (Proposal) -> Unit,
    private val onScoreClick: ((Proposal) -> Unit)? = null,
    private val onContactClick: ((Proposal) -> Unit)? = null
) : RecyclerView.Adapter<ProposalAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProposalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProposalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context
        val role = SessionManager(context).getUserRole()

        holder.binding.apply {
            tvUserName.text = item.userName.ifEmpty { "Inovator" }
            tvUniversity.text = item.userUniversity.ifEmpty { "Universitas" }
            tvJudulSolusi.text = item.judul
            tvStatusBadge.text = item.status
            
            if (item.score > 0) {
                tvScoreVal.text = item.score.toString()
            } else {
                tvScoreVal.text = "-"
            }

            // Dynamic status badge coloring
            val statusBgColor = when (item.status.lowercase(Locale.getDefault())) {
                "diterima" -> R.color.status_diterima_bg
                "ditolak" -> R.color.status_ditolak_bg
                "review" -> R.color.status_review_bg
                else -> R.color.status_proses_bg // Pending
            }
            val statusTextColor = when (item.status.lowercase(Locale.getDefault())) {
                "diterima" -> R.color.status_diterima_text
                "ditolak" -> R.color.status_ditolak_text
                "review" -> R.color.status_review_text
                else -> R.color.status_proses_text // Pending
            }
            tvStatusBadge.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(context, statusBgColor)
            tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(context, statusTextColor))
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvTanggal.text = "Submitted " + sdf.format(Date(item.tanggal))
            
            btnDetail.setOnClickListener { onDetailClick(item) }
            
            if (role == Constants.ROLE_PERUSAHAAN) {
                btnGiveScore.visibility = View.VISIBLE
                btnContact.visibility = View.VISIBLE
                
                btnGiveScore.setOnClickListener { onScoreClick?.invoke(item) }
                btnContact.setOnClickListener { onContactClick?.invoke(item) }
            } else {
                btnGiveScore.visibility = View.GONE
                btnContact.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Proposal>) {
        list = newList
        notifyDataSetChanged()
    }
}
