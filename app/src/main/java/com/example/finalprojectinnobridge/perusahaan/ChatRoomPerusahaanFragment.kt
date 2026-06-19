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
import com.example.finalprojectinnobridge.adapters.ChatRoomAdapter
import com.example.finalprojectinnobridge.databinding.FragmentChatRoomBinding
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.utils.SessionManager
import com.example.finalprojectinnobridge.viewmodels.MessageViewModel

class ChatRoomPerusahaanFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MessageViewModel by viewModels()
    private lateinit var adapter: ChatRoomAdapter
    private var myId: String = ""
    private var myName: String = ""
    private var partnerId: String = ""
    private var partnerName: String = ""

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
        myName = sessionManager.getUserName() ?: "Perusahaan"

        // Ambil args — gunakan requireArguments() agar jelas jika null
        partnerId = arguments?.getString("partnerId")?.takeIf { it.isNotEmpty() } ?: ""
        partnerName = arguments?.getString("partnerName")?.takeIf { it.isNotEmpty() }
            ?: "Mahasiswa"

        if (myId.isEmpty()) {
            showErrorAndGoBack("Sesi habis. Silakan login kembali.")
            return
        }

        if (partnerId.isEmpty()) {
            showErrorAndGoBack("Kontak tidak valid.")
            return
        }

        // Bind custom WA-style header
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
        adapter = ChatRoomAdapter(myId, showAvatars = true)
        binding.rvChatRoom.apply {
            layoutManager = LinearLayoutManager(requireContext()).also {
                it.stackFromEnd = true
            }
            adapter = this@ChatRoomPerusahaanFragment.adapter
        }
    }

    private fun observeMessages() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            if (!messages.isNullOrEmpty()) {
                adapter.submitList(messages.toList()) {
                    // Scroll ke bawah setelah list terupdate
                    binding.rvChatRoom.scrollToPosition(messages.size - 1)
                }
            } else {
                adapter.submitList(emptyList())
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim() ?: return@setOnClickListener
            if (text.isEmpty()) return@setOnClickListener

            val message = Message(
                senderId = myId,
                senderName = myName,
                receiverId = partnerId,
                receiverName = partnerName,
                message = text,
                timestamp = System.currentTimeMillis()
            )

            viewModel.sendMessage(message) { success, err ->
                activity?.runOnUiThread {
                    if (success) {
                        binding.etMessage.text?.clear()
                    } else {
                        Toast.makeText(requireContext(), "Gagal kirim: $err", Toast.LENGTH_SHORT)
                            .show()
                    }
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
