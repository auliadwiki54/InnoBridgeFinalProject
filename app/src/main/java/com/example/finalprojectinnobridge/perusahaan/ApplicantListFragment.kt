package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.adapters.ApplicantAdapter
import com.example.finalprojectinnobridge.adapters.ApplicantListItem
import com.example.finalprojectinnobridge.databinding.FragmentApplicantListBinding
import com.example.finalprojectinnobridge.repositories.UserRepository
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.ChallengeViewModel
import com.example.finalprojectinnobridge.viewmodels.ProposalViewModel

class ApplicantListFragment : Fragment() {

    private var _binding: FragmentApplicantListBinding? = null
    private val binding get() = _binding!!

    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val proposalViewModel: ProposalViewModel by viewModels()
    private lateinit var applicantAdapter: ApplicantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        loadApplicantsData()
    }

    private fun setupRecyclerView() {
        applicantAdapter = ApplicantAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putString("partnerId", item.userId)
                putString("partnerName", item.name)
            }
            try {
                findNavController().navigate(R.id.action_applicant_list_to_chat_room, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.rvApplicants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicantAdapter
        }
    }

    private fun loadApplicantsData() {
        val companyId = SessionManager(requireContext()).getUserId() ?: ""
        if (companyId.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        challengeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            val myChallengeIds = challenges.filter { it.perusahaanId == companyId }.map { it.challengeId }

            proposalViewModel.proposals.observe(viewLifecycleOwner) { allProposals ->
                val myProposals = allProposals.filter { it.challengeId in myChallengeIds }

                // Group proposals by student userId
                val grouped = myProposals.groupBy { it.userId }

                val listItems = mutableListOf<ApplicantListItem>()
                val userRepo = UserRepository()

                var loadedCount = 0
                val totalUsersToLoad = grouped.keys.size

                if (totalUsersToLoad == 0) {
                    if (isAdded && _binding != null) {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvApplicants.visibility = View.GONE
                        applicantAdapter.updateData(emptyList())
                    }
                    return@observe
                }

                for ((uid, proposals) in grouped) {
                    val sampleProposal = proposals.first()
                    val proposalCount = proposals.size
                    val totalScore = proposals.sumOf { it.score }

                    userRepo.getUserData(uid) { user, _ ->
                        val item = ApplicantListItem(
                            userId = uid,
                            name = user?.nama?.takeIf { it.isNotEmpty() } ?: sampleProposal.userName,
                            university = user?.universitas?.takeIf { it.isNotEmpty() } ?: sampleProposal.userUniversity,
                            department = user?.jurusan ?: "",
                            skill = user?.keahlian ?: "",
                            proposalCount = proposalCount,
                            totalScore = totalScore,
                            photoUrl = user?.photoUrl ?: ""
                        )
                        synchronized(listItems) {
                            listItems.add(item)
                        }

                        activity?.runOnUiThread {
                            if (isAdded && _binding != null) {
                                loadedCount++
                                if (loadedCount == totalUsersToLoad) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.layoutEmptyState.visibility = if (listItems.isEmpty()) View.VISIBLE else View.GONE
                                    binding.rvApplicants.visibility = if (listItems.isEmpty()) View.GONE else View.VISIBLE
                                    // Sort by proposal count, then total score
                                    applicantAdapter.updateData(listItems.sortedWith(
                                        compareByDescending<ApplicantListItem> { it.proposalCount }
                                            .thenByDescending { it.totalScore }
                                    ))
                                }
                            }
                        }
                    }
                }
            }
        }

        challengeViewModel.fetchChallenges()
        proposalViewModel.listenToAllProposals()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
