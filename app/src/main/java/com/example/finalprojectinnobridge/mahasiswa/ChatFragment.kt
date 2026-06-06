package com.example.finalprojectinnobridge.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var messageAdapter: MessageAdapter
    private var receiverId: String? = "company_dummy_id" // In real app, pass via args

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

        setupRecyclerView(currentUserId)
        observeViewModel()

        receiverId?.let { 
            viewModel.fetchMessages(currentUserId, it)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && receiverId != null) {
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId!!,
                    message = text,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.sendMessage(message) { success, error ->
                    if (success) {
                        binding.etMessage.text.clear()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(currentUserId: String) {
        messageAdapter = MessageAdapter(emptyList(), currentUserId)
        binding.rvChat.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.updateData(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}