package com.example.project.kamar_admin.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Pasien
import com.example.project.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.widget.Toast

class PasienAdapter(
    private val pasienList: List<Pasien>,
    private val onPindahRuanganClick: (Pasien) -> Unit,
) : RecyclerView.Adapter<PasienAdapter.PasienViewHolder>() {

    inner class PasienViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaPasien: TextView = itemView.findViewById(R.id.tvNamaPasien)
        val penyakit: TextView = itemView.findViewById(R.id.tvPenyakit)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val tanggalMasuk: TextView = itemView.findViewById(R.id.tvTanggalMasuk)
        val btnPindahRuangan: Button = itemView.findViewById(R.id.btnPindahRuangan)
        val btnPerluPerhatian: Button = itemView.findViewById(R.id.btnPerluPerhatian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasienViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pasien, parent, false)
        return PasienViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasienViewHolder, position: Int) {
        val pasien = pasienList[position]
        holder.namaPasien.text = pasien.nama_Pasien
        holder.penyakit.text = pasien.penyakit
        holder.status.text = pasien.status

        // Format timestamp to readable date
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = Date(pasien.tanggal_Masuk?.toLong() ?: 0L)
        holder.tanggalMasuk.text = sdf.format(date)

        holder.btnPindahRuangan.setOnClickListener { onPindahRuanganClick(pasien) }

        holder.btnPerluPerhatian.setOnClickListener {
            sendMedicalAlert(holder.itemView.context, pasien)
        }
    }

    private fun sendMedicalAlert(context: Context, pasien: Pasien) {
        val db = FirebaseDatabase.getInstance().reference
        val notificationId = db.child("notifications").push().key ?: return

        val notification = hashMapOf(
            "isRead" to false,
            "timestamp" to System.currentTimeMillis(),
            "message" to "Pasien ${pasien.nama_Pasien} memerlukan perhatian medis segera!",
            "type" to "medical_alert",
            "details" to mapOf(
                "namaPasien" to pasien.nama_Pasien,
                "penyakit" to pasien.penyakit,
                "status" to pasien.status,
                "ruangan" to pasien.nama_Ruangan,
                "tanggalMasuk" to pasien.tanggal_Masuk
            )
        )

        db.child("notifications").child(notificationId).setValue(notification)
            .addOnSuccessListener {
                // Menampilkan Toast ketika pengiriman berhasil
                Toast.makeText(
                    context,
                    "Notifikasi untuk ${pasien.nama_Pasien} berhasil dikirimkan.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                // Menampilkan Toast ketika pengiriman gagal
                Toast.makeText(
                    context,
                    "Gagal mengirim notifikasi: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun getItemCount(): Int = pasienList.size
}