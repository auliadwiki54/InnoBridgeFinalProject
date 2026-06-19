package com.example.finalprojectinnobridge.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalprojectinnobridge.databinding.FragmentChatRoomBinding
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.MessageViewModel

class ChatRoomFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MessageViewModel by viewModels()
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var myId: String
    private lateinit var myName: String
    private lateinit var partnerId: String
    private lateinit var partnerName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        myId = sessionManager.getUserId() ?: ""
        myName = sessionManager.getUserName() ?: "User"
        partnerId = arguments?.getString("partnerId") ?: ""
        partnerName = arguments?.getString("partnerName") ?: ""

        // Validate essential data
        if (myId.isEmpty()) {
            showErrorAndGoBack("User ID tidak ditemukan. Silakan login kembali.")
            return
        }

        if (partnerId.isEmpty()) {
            showErrorAndGoBack("Partner ID tidak ditemukan. Silakan pilih partner yang valid.")
            return
        }

        // Bind custom WA-style header
        if (partnerName.isEmpty()) {
            partnerName = "User ($partnerId)"
        }
        binding.tvChatName.text = partnerName
        binding.tvChatStatus.text = "Mitra InnoBridge"
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Fetch partner details from Firestore to show dynamic avatar
        com.example.finalprojectinnobridge.repositories.UserRepository().getUserData(partnerId) { user, _ ->
            if (isAdded && _binding != null) {
                user?.let { u ->
                    if (u.nama.isNotEmpty()) {
                        partnerName = u.nama
                        binding.tvChatName.text = partnerName
                    }
                    if (u.photoUrl.isNotEmpty()) {
                        binding.ivChatAvatar.visibility = View.VISIBLE
                        binding.tvChatAvatarInitial.visibility = View.GONE
                        com.bumptech.glide.Glide.with(this)
                            .load(u.photoUrl)
                            .placeholder(com.example.finalprojectinnobridge.R.color.secondary_blue)
                            .into(binding.ivChatAvatar)
                    } else {
                        binding.ivChatAvatar.visibility = View.GONE
                        binding.tvChatAvatarInitial.visibility = View.VISIBLE
                        binding.tvChatAvatarInitial.text = if (partnerName.isNotEmpty()) {
                            partnerName.first().uppercaseChar().toString()
                        } else {
                            "?"
                        }
                    }
                } ?: run {
                    binding.ivChatAvatar.visibility = View.GONE
                    binding.tvChatAvatarInitial.visibility = View.VISIBLE
                    binding.tvChatAvatarInitial.text = if (partnerName.isNotEmpty()) {
                        partnerName.first().uppercaseChar().toString()
                    } else {
                        "?"
                    }
                }
            }
        }

        setupRecyclerView()
        observeMessages()
        setupSendButton()

        viewModel.fetchMessages(myId, partnerId)
    }

    private fun setupRecyclerView() {
        adapter = ChatRoomAdapter(myId)
        binding.rvChatRoom.apply {
            layoutManager = LinearLayoutManager(requireContext()).also {
                it.stackFromEnd = true
            }
            adapter = this@ChatRoomFragment.adapter
        }
    }

    private fun observeMessages() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            if (messages != null) {
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.rvChatRoom.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Pesan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = Message(
                senderId = myId,
                senderName = myName,
                receiverId = partnerId,
                receiverName = partnerName,
                message = text,
                timestamp = System.currentTimeMillis()
            )

            viewModel.sendMessage(message) { success, err ->
                if (success) {
                    binding.etMessage.text?.clear()
                } else {
                    Toast.makeText(requireContext(), "Gagal kirim: $err", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showErrorAndGoBack(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}