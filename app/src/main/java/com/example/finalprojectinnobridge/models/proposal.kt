package com.example.finalprojectinnobridge.models

data class Proposal(
    val proposalId: String = "",
    val challengeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userUniversity: String = "",
    val judul: String = "",
    val solusi: String = "",
    val status: String = "Pending", // Pending, Review, Diterima, Ditolak
    val tanggal: Long = System.currentTimeMillis(),
    val pitchVideo: String = "",
    val pdfUrl: String = "", // URL to the uploaded PDF file
    val score: Int = 0,
    val evaluasi: String = ""
)