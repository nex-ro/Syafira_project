package com.example.project.Data

data class Laporan_Penanganan(
    val nama_Pasien: String? = null,
    val nama_Ruangan: String? = null,
    val penyakit: String? = null,
    val status: String? = null,
    val tanggal_Masuk: Long? = null, // Ganti nama dari "tanggalMasuk" ke "tanggal_Masuk"
    var jenis: String? = null // Ditambahkan untuk mapping jenis

)
