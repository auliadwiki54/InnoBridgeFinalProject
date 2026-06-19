package com.example.finalprojectinnobridge.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.finalprojectinnobridge.R
import com.example.finalprojectinnobridge.activities.MainActivity
import com.example.finalprojectinnobridge.models.Message
import com.example.finalprojectinnobridge.models.Proposal
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentChange
import java.util.Locale

object NotificationHelper {

    private const val CHANNEL_CHAT_ID = "chat_channel"
    private const val CHANNEL_PROPOSAL_ID = "proposal_channel"

    private var chatListener: ValueEventListener? = null
    private var proposalsListener: ListenerRegistration? = null
    private var studentProposalsListener: ListenerRegistration? = null

    private val seenMessageIds = mutableSetOf<String>()
    private val seenProposalIds = mutableSetOf<String>()
    private val studentProposalCache = mutableMapOf<String, Pair<String, Int>>() // proposalId -> Pair(status, score)

    private var isFirstChatLoad = true
    private var isFirstProposalLoad = true
    private var isFirstStudentProposalLoad = true

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chatChannel = NotificationChannel(
                CHANNEL_CHAT_ID,
                "Pesan Chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk pesan chat masuk"
            }

            val proposalChannel = NotificationChannel(
                CHANNEL_PROPOSAL_ID,
                "Proposal & Nilai",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk proposal masuk dan pemberian nilai"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chatChannel)
            manager.createNotificationChannel(proposalChannel)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val manager = NotificationManagerCompat.from(context)

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            try {
                manager.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun startListening(context: Context, currentUserId: String, role: String) {
        // Stop any active listeners first to clean up state
        stopListening()

        // 1. Listen for Chat Messages (For both Mahasiswa and Perusahaan)
        val databaseRef = FirebaseDatabase.getInstance().reference.child("messages")
        seenMessageIds.clear()
        isFirstChatLoad = true

        chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessagesToNotify = mutableListOf<Message>()

                for (roomSnapshot in snapshot.children) {
                    for (msgSnapshot in roomSnapshot.children) {
                        val msg = msgSnapshot.getValue(Message::class.java) ?: continue
                        val msgId = msg.messageId.ifEmpty { msgSnapshot.key ?: "" }
                        if (msgId.isEmpty()) continue

                        if (isFirstChatLoad) {
                            seenMessageIds.add(msgId)
                        } else {
                            if (msg.receiverId == currentUserId && !seenMessageIds.contains(msgId)) {
                                seenMessageIds.add(msgId)
                                newMessagesToNotify.add(msg)
                            }
                        }
                    }
                }

                if (isFirstChatLoad) {
                    isFirstChatLoad = false
                } else {
                    for (msg in newMessagesToNotify) {
                        showNotification(
                            context,
                            CHANNEL_CHAT_ID,
                            msgIdHashCode(msg.messageId),
                            "Pesan Baru dari ${msg.senderName}",
                            msg.message
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        databaseRef.addValueEventListener(chatListener!!)

        // 2. Role-specific listeners
        val db = FirebaseFirestore.getInstance()

        if (role == Constants.ROLE_PERUSAHAAN) {
            // Listen for new incoming proposals for this company
            db.collection(Constants.CHALLENGES_COLLECTION)
                .whereEqualTo("perusahaanId", currentUserId)
                .get()
                .addOnSuccessListener { challengeSnapshots ->
                    val companyChallengeIds = challengeSnapshots.documents.mapNotNull { it.id }.toSet()
                    if (companyChallengeIds.isEmpty()) return@addOnSuccessListener

                    seenProposalIds.clear()
                    isFirstProposalLoad = true

                    proposalsListener = db.collection(Constants.PROPOSALS_COLLECTION)
                        .addSnapshotListener { snapshots, e ->
                            if (e != null || snapshots == null) return@addSnapshotListener

                            val newProposalsToNotify = mutableListOf<Proposal>()

                            for (change in snapshots.documentChanges) {
                                val proposal = change.document.toObject(Proposal::class.java)
                                val proposalId = change.document.id

                                if (proposal.challengeId in companyChallengeIds) {
                                    if (change.type == DocumentChange.Type.ADDED) {
                                        if (isFirstProposalLoad) {
                                            seenProposalIds.add(proposalId)
                                        } else {
                                            if (!seenProposalIds.contains(proposalId)) {
                                                seenProposalIds.add(proposalId)
                                                newProposalsToNotify.add(proposal)
                                            }
                                        }
                                    }
                                }
                            }

                            if (isFirstProposalLoad) {
                                isFirstProposalLoad = false
                            } else {
                                for (p in newProposalsToNotify) {
                                    showNotification(
                                        context,
                                        CHANNEL_PROPOSAL_ID,
                                        p.hashCode(),
                                        "Proposal Baru Masuk",
                                        "${p.userName} mengirim solusi '${p.judul}'"
                                    )
                                }
                            }
                        }
                }
        } else if (role == Constants.ROLE_MAHASISWA) {
            // Listen for status or score changes for this student
            studentProposalCache.clear()
            isFirstStudentProposalLoad = true

            studentProposalsListener = db.collection(Constants.PROPOSALS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null || snapshots == null) return@addSnapshotListener

                    val updatesToNotify = mutableListOf<Proposal>()

                    for (change in snapshots.documentChanges) {
                        val proposal = change.document.toObject(Proposal::class.java)
                        val proposalId = change.document.id
                        val currentStatus = proposal.status
                        val currentScore = proposal.score

                        if (isFirstStudentProposalLoad) {
                            studentProposalCache[proposalId] = Pair(currentStatus, currentScore)
                        } else {
                            if (change.type == DocumentChange.Type.MODIFIED ||
                                change.type == DocumentChange.Type.ADDED) {

                                val cached = studentProposalCache[proposalId]
                                if (cached != null) {
                                    val (prevStatus, prevScore) = cached
                                    if (currentStatus != prevStatus || currentScore != prevScore) {
                                        studentProposalCache[proposalId] = Pair(currentStatus, currentScore)
                                        updatesToNotify.add(proposal)
                                    }
                                } else {
                                    studentProposalCache[proposalId] = Pair(currentStatus, currentScore)
                                }
                            }
                        }
                    }

                    if (isFirstStudentProposalLoad) {
                        isFirstStudentProposalLoad = false
                    } else {
                        for (p in updatesToNotify) {
                            val msg = if (p.score > 0) {
                                "Proposal '${p.judul}' telah dinilai dengan skor ${p.score} (Status: ${p.status})"
                            } else {
                                "Status proposal '${p.judul}' diperbarui menjadi ${p.status}"
                            }
                            showNotification(
                                context,
                                CHANNEL_PROPOSAL_ID,
                                p.hashCode(),
                                "Pembaruan Proposal",
                                msg
                            )
                        }
                    }
                }
        }
    }

    fun stopListening() {
        chatListener?.let {
            FirebaseDatabase.getInstance().reference.child("messages").removeEventListener(it)
            chatListener = null
        }
        proposalsListener?.let {
            it.remove()
            proposalsListener = null
        }
        studentProposalsListener?.let {
            it.remove()
            studentProposalsListener = null
        }
    }

    private fun msgIdHashCode(msgId: String): Int {
        return if (msgId.isEmpty()) System.currentTimeMillis().toInt() else msgId.hashCode()
    }
}
