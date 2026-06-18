package com.example.finalprojectinnobridge.utils

import com.example.finalprojectinnobridge.firebase.FirebaseManager
import com.example.finalprojectinnobridge.models.Challenge
import com.example.finalprojectinnobridge.models.Message
import com.google.firebase.database.FirebaseDatabase

object SimulasiData {

    fun generateSimulasiTantangan() {
        val db = FirebaseManager.getInstance().db
        val challenges = listOf(
            Challenge(
                judul = "Inovasi Energi Surya untuk Desa Terpencil",
                deskripsi = "Menciptakan sistem panel surya portabel yang murah dan mudah dirawat oleh masyarakat desa.",
                targetPeserta = "Mahasiswa Teknik Elektro, Fisika",
                kategori = "SDG 7: Energi Bersih",
                skemaLisensi = "Open Collaboration",
                reward = "Rp 15.000.000",
                deadline = "20 Desember 2024",
                perusahaanId = "company_pertamina",
                status = "Aktif",
                imageUrl = "img_sdg7_solar.jpg"
            ),
            Challenge(
                judul = "Sistem IoT Monitoring Kualitas Air Sungai",
                deskripsi = "Membangun sensor IoT yang dapat mendeteksi polusi limbah secara real-time di sungai perkotaan.",
                targetPeserta = "Mahasiswa IT, Teknik Lingkungan",
                kategori = "SDG 11: Kota Berkelanjutan",
                skemaLisensi = "Revenue Share",
                reward = "Rp 25.000.000",
                deadline = "15 Januari 2025",
                perusahaanId = "company_pdam",
                status = "Aktif",
                imageUrl = "img_sdg11_waste.jpg"
            ),
            Challenge(
                judul = "Platform Digital Marketplace Sampah Plastik",
                deskripsi = "Aplikasi untuk menghubungkan rumah tangga dengan bank sampah secara efisien.",
                targetPeserta = "UI/UX Designer, Mobile Developer",
                kategori = "SDG 12: Konsumsi Bertanggung Jawab",
                skemaLisensi = "Eksklusif",
                reward = "Pendanaan Seed Rp 50jt",
                deadline = "30 Desember 2024",
                perusahaanId = "company_waste4change",
                status = "Aktif",
                imageUrl = "img_sdg12_waste.jpg"
            ),
            Challenge(
                judul = "Restorasi Terumbu Karang Berbasis AI",
                deskripsi = "Algoritma untuk memantau kesehatan terumbu karang melalui citra bawah laut.",
                targetPeserta = "Data Scientist, Ilmu Kelautan",
                kategori = "SDG 14: Ekosistem Laut",
                skemaLisensi = "Open Collaboration",
                reward = "Trip Riset ke Bunaken",
                deadline = "10 Februari 2025",
                perusahaanId = "company_kkp",
                status = "Aktif",
                imageUrl = "img_sdg14_coral.jpg"
            )
        )

        challenges.forEach { challenge ->
            db.collection(Constants.CHALLENGES_COLLECTION)
                .document(challenge.judul.replace(" ", "_").lowercase())
                .set(challenge)
        }
    }

    fun generateSimulasiChat(myId: String) {
        val db = FirebaseDatabase.getInstance().reference.child("messages")
        
        val partners = listOf(
            Pair("company_pertamina", "Pertamina Inovasi"),
            Pair("company_pdam", "PDAM Tirta"),
            Pair("company_kkp", "Kementerian Kelautan")
        )

        partners.forEach { (pId, pName) ->
            val roomId = if (myId < pId) "${myId}_$pId" else "${pId}_$myId"
            
            val messages = listOf(
                Message(
                    senderId = pId,
                    senderName = pName,
                    receiverId = myId,
                    receiverName = "Mahasiswa",
                    message = "Halo! Kami sudah melihat profil Anda. Apakah tertarik dengan tantangan kami?",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                Message(
                    senderId = myId,
                    senderName = "Mahasiswa",
                    receiverId = pId,
                    receiverName = pName,
                    message = "Halo $pName, ya saya sangat tertarik. Saya sedang menyusun proposalnya.",
                    timestamp = System.currentTimeMillis() - 43200000
                ),
                Message(
                    senderId = pId,
                    senderName = pName,
                    receiverId = myId,
                    receiverName = "Mahasiswa",
                    message = "Bagus! Jangan lupa sertakan file PDF teknisnya ya.",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            messages.forEach { msg ->
                db.child(roomId).push().setValue(msg)
            }
        }
    }
}