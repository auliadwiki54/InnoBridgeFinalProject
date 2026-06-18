package com.example.finalprojectinnobridge.models

data class Challenge(
    val challengeId: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val targetPeserta: String = "",
    val kategori: String = "", // Target SDGs
    val skemaLisensi: String = "",
    val reward: String = "",
    val deadline: String = "",
    val perusahaanId: String = "",
    val status: String = "Aktif",
    val imageUrl: String = ""
)