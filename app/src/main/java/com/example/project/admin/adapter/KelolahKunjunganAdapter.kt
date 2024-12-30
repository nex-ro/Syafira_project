package com.example.project.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Kunjungan
import com.example.project.R
import java.text.SimpleDateFormat
import java.util.*

class KelolahKunjunganAdapter(
    private var kunjunganList: List<Kunjungan>,
    private val onTerimaClick: (Kunjungan, String) -> Unit,
    private val onTolakClick: (Kunjungan, String) -> Unit
) : RecyclerView.Adapter<KelolahKunjunganAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaPengunjung: TextView = view.findViewById(R.id.namaPasien)
        val ruangPasien: TextView = view.findViewById(R.id.ruangPasien)
        val kelolahNama: TextView = view.findViewById(R.id.kelolah_nama)
        val kelolahKamar: TextView = view.findViewById(R.id.kelolah_kamar)
        val kelolahTgl: TextView = view.findViewById(R.id.kelolahtgl)
        val kelolahJam: TextView = view.findViewById(R.id.kelolahjam)
        val kelolahHubungan: TextView = view.findViewById(R.id.kelolahHubungan)
        val inputKeterangan: EditText = view.findViewById(R.id.inputan_keterangan)
        val btnTerima: Button = view.findViewById(R.id.btnTerima)
        val btnTolak: Button = view.findViewById(R.id.btnTolak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_kelolah_kunjungan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val kunjungan = kunjunganList[position]

            holder.apply {
                namaPengunjung.text = kunjungan.nama ?: "-"
                ruangPasien.text = "Pengunjung"
                kelolahNama.text = "Nama Pasien : ${kunjungan.nama_pasien ?: "-"}"
                kelolahKamar.text = "Kamar Pasien : ${kunjungan.kamar_pasien ?: "-"}"
                kelolahTgl.text = "Tanggal Kunjungan : ${formatDate(kunjungan.tanggal_kunjungan)}"
                kelolahJam.text = "Jam Kunjungan : ${kunjungan.jam_kunjungan ?: "-"}"
                kelolahHubungan.text = "Hubungan dengan pasien : ${kunjungan.hubungan ?: "-"}"

                btnTerima.setOnClickListener {
                    val keterangan = inputKeterangan.text.toString()
                    onTerimaClick(kunjungan, keterangan)
                }

                btnTolak.setOnClickListener {
                    val keterangan = inputKeterangan.text.toString()
                    onTolakClick(kunjungan, keterangan)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItemCount() = kunjunganList.size

    fun updateData(newList: List<Kunjungan>) {
        kunjunganList = newList
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Format tidak valid"
        }
    }
}