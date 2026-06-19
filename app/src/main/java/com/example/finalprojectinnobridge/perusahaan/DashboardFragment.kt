package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ChallengeAdapter
import com.example.finalprojectinnobridge.databinding.FragmentDashboardBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val proposalViewModel: ProposalViewModel by viewModels()
    private lateinit var challengeAdapter: ChallengeAdapter

    private val myChallengeIds = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val companyName = SessionManager(requireContext()).getUserName() ?: "Perusahaan"
        binding.tvCompanyName.text = companyName

        binding.btnCreateChallengeHeader.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_challenge)
        }
        binding.btnCreateChallengeEmpty.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_challenge)
        }

        setupRecyclerView()
        setupSearchView()
        setupStatCardClicks()
        observeViewModel()

        challengeViewModel.fetchChallenges()
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList()) { challenge ->
            val bundle = Bundle().apply {
                putString("challengeId", challenge.challengeId)
            }
            findNavController().navigate(R.id.navigation_edit_challenge, bundle)
        }

        binding.rvMyChallenges.apply {
            adapter = challengeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyChallengesFilter(newText ?: "")
                return true
            }
        })
    }

    private fun setupStatCardClicks() {
        binding.cvActiveChallenges.setOnClickListener {
            // Premium scale bounce animation on tap
            binding.cvActiveChallenges.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                binding.cvActiveChallenges.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }.start()
            Toast.makeText(requireContext(), "Menampilkan semua tantangan Anda", Toast.LENGTH_SHORT).show()
        }

        binding.cvTotalApplicants.setOnClickListener {
            binding.cvTotalApplicants.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                binding.cvTotalApplicants.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).withEndAction {
                    try {
                        findNavController().navigate(R.id.action_dashboard_to_applicant_list)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }.start()
        }

        binding.cvPendingReview.setOnClickListener {
            binding.cvPendingReview.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                binding.cvPendingReview.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).withEndAction {
                    try {
                        findNavController().navigate(R.id.navigation_proposal_list)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }.start()
        }
    }

    private fun applyChallengesFilter(searchQuery: String = binding.searchView.query?.toString() ?: "") {
        val userId = SessionManager(requireContext()).getUserId() ?: ""
        val allChallenges = challengeViewModel.challenges.value ?: emptyList()
        val myChallenges = allChallenges.filter { it.perusahaanId == userId }

        var filtered = myChallenges

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.judul.contains(searchQuery, ignoreCase = true) ||
                it.deskripsi.contains(searchQuery, ignoreCase = true)
            }
        }

        challengeAdapter.updateData(filtered)
    }

    private fun observeViewModel() {
        val userId = SessionManager(requireContext()).getUserId() ?: ""

        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallenges = challenges.filter { it.perusahaanId == userId }
            val isEmpty = myChallenges.isEmpty()
            binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvMyChallenges.visibility = if (isEmpty) View.GONE else View.VISIBLE

            myChallengeIds.clear()
            myChallengeIds.addAll(myChallenges.map { it.challengeId })

            binding.tvActiveCount.text = myChallenges.size.toString()

            applyChallengesFilter()
        }

        proposalViewModel.proposals.observe(viewLifecycleOwner) { proposals ->
            val myProposals = proposals.filter { it.challengeId in myChallengeIds }
            val totalApplicants = myProposals.size
            binding.tvTotalApplicantsCount.text = totalApplicants.toString()

            val pendingReviewCount = myProposals.count { it.status == "Pending" || it.status == "Review" }
            binding.tvPendingCount.text = pendingReviewCount.toString()
        }
        
        proposalViewModel.listenToAllProposals()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}