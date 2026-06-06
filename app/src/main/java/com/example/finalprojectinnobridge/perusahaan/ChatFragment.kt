package com.example.finalprojectinnobridge.perusahaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.adapters.MessageAdapter
import com.example.finalprojectinnobridge.databinding.FragmentChatBinding
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.MessageViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MessageViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val currentUserId = sessionManager.getUserId() ?: ""
        
        // In a real app, the receiverId would be passed as an argument (e.g., from a list of applicants)
        val receiverId = arguments?.getString("receiverId") ?: "student_123" 

        setupRecyclerView(currentUserId)
        observeViewModel()

        viewModel.fetchMessages(currentUserId, receiverId)

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotEmpty()) {
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    message = text
                )
                viewModel.sendMessage(message) { _, _ ->
                    binding.etMessage.setText("")
                }
            }
        }
    }

    private fun setupRecyclerView(userId: String) {
        adapter = MessageAdapter(emptyList(), userId)
        binding.rvChat.layoutManager = LinearLayoutManager(context)
        binding.rvChat.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            if (it.isNotEmpty()) {
                binding.rvChat.scrollToPosition(it.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}