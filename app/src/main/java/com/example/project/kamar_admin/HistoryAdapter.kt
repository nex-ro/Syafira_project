package com.example.project.kamar_admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.History
import com.example.project.R
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private var historyList: List<History>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoading: Boolean = true

    // ViewHolder untuk data sebenarnya
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvPenyakit: TextView = itemView.findViewById(R.id.tvPenyakit)
        val tvTanggalMasuk: TextView = itemView.findViewById(R.id.tvTanggalMasuk)
        val tvTanggalKeluar: TextView = itemView.findViewById(R.id.tvTanggalKeluar)
    }

    // ViewHolder untuk shimmer
    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val shimmerView = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_history_shadow, parent, false)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_history, parent, false)
            HistoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryViewHolder) {
            val history = historyList[position]

            holder.tvNama.text = "Nama: ${history.nama ?: "Tidak tersedia"}"
            holder.tvPenyakit.text = "Penyakit: ${history.penyakit ?: "Tidak tersedia"}"
            holder.tvTanggalMasuk.text = "Tanggal Masuk: ${formatTimestamp(history.tanggal_Masuk)}"
            holder.tvTanggalKeluar.text = "Tanggal Keluar: ${formatTimestamp(history.tanggal_Keluar)}"
        } else if (holder is ShimmerViewHolder) {
            // Shimmer sedang berjalan, tidak ada logika lain di sini
        }
    }

    override fun getItemCount(): Int = if (isLoading) 5 else historyList.size

    // Aktifkan atau nonaktifkan mode loading
    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: String?): String {
        return try {
            if (!timestamp.isNullOrEmpty()) {
                val date = Date(timestamp.toLong())
                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                format.format(date)
            } else {
                "Tidak tersedia"
            }
        } catch (e: Exception) {
            "Format tidak valid"
        }
    }
}
