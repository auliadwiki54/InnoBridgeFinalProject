package com.example.finalprojectinnobridge.mahasiswa

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
import com.example.finalprojectinnobridge.adapters.ChatListAdapter
import com.example.finalprojectinnobridge.databinding.FragmentChatBinding
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.MessageViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MessageViewModel by viewModels()
    private lateinit var chatListAdapter: ChatListAdapter
    private var myId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myId = SessionManager(requireContext()).getUserId() ?: ""

        if (myId.isEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        observeChatInbox()
    }

    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter(emptyList(), myId) { partnerId, partnerName ->
            if (partnerId.isEmpty()) {
                Toast.makeText(requireContext(), "ID Partner tidak valid", Toast.LENGTH_SHORT).show()
                return@ChatListAdapter
            }

            val bundle = Bundle().apply {
                putString("partnerId", partnerId)
                putString("partnerName", partnerName.ifEmpty { "User ($partnerId)" })
            }
            try {
                findNavController().navigate(R.id.action_navigation_chat_to_navigation_chat_room, bundle)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membuka chat: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatListAdapter
        }
    }

    private fun observeChatInbox() {
        viewModel.fetchMessages(myId, "")
        viewModel.messages.observe(viewLifecycleOwner) { listPesan ->
            if (listPesan != null && listPesan.isNotEmpty()) {
                // Group messages by partner (sender or receiver)
                val latestMessages = listPesan.groupBy {
                    if (it.senderId == myId) it.receiverId else it.senderId
                }.mapNotNull { (partnerId, messages) ->
                    if (partnerId.isNotEmpty() && messages.isNotEmpty()) {
                        messages.last() // Get latest message from this partner
                    } else {
                        null
                    }
                }.sortedByDescending { it.timestamp }

                chatListAdapter.updateData(latestMessages)
                binding.rvChatList.visibility = if (latestMessages.isEmpty()) View.GONE else View.VISIBLE
            } else {
                binding.rvChatList.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}