package com.example.project.Data

data class Ruangan(
    val id_Ruangan: String? = null,
    val nomor_Ruangan: Int? = null,
    val jenis: String? = null,
    val nama_Ruangan: String? = null,
    val status: String? = null,
    val kapasitas:Int?=1,
    val isi:Int?=0,
    val lantai: Int?=null,
)
