package com.example.finalprojectinnobridge.models

data class User(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "", // Mahasiswa or Perusahaan
    val photoUrl: String = "",
    val bio: String = ""
)