package com.example.project.Data

data class History(
    val nama: String? = null,
    val penyakit: String? = null,
    val tanggal_Masuk: Long = 0L,  // Change to Long
    val tanggal_Keluar: Long = 0L // Change to Long
)
