package com.example.project.admin.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.History
import com.example.project.Data.Kunjungan
import com.example.project.R
import java.text.SimpleDateFormat
import java.util.*

class history_kj_Adapter(private var kunjungan: List<Kunjungan>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoading: Boolean = true

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.hsKunjunganNama)
        val pasien: TextView = itemView.findViewById(R.id.hsKunjunganPasien)
        val kamar: TextView = itemView.findViewById(R.id.hsKunjungankamar)
        val tgl: TextView = itemView.findViewById(R.id.hsKunjunganTgl)
        val jam: TextView = itemView.findViewById(R.id.hsKunjunganjam)
        val status: TextView = itemView.findViewById(R.id.hsKunjunganStatus)
        val ket: TextView = itemView.findViewById(R.id.hsKunjunganKet)
    }

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
                .inflate(R.layout.card_histoy_kunjungan, parent, false)
            HistoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryViewHolder && !isLoading) {
            val kunjunganItem = kunjungan[position]

            holder.apply {
                nama.text = "Nama: ${kunjunganItem.nama ?: "Tidak tersedia"}"
                pasien.text = "Pasien dikunjungi: ${kunjunganItem.nama_pasien ?: "Tidak tersedia"}"
                kamar.text = "Kamar: ${kunjunganItem.kamar_pasien ?: "Tidak tersedia"}"
                tgl.text = "Tanggal Kunjungan: ${formatDate(kunjunganItem.tanggal_kunjungan)}"
                jam.text = "Jam Kunjungan: ${kunjunganItem.jam_kunjungan}"
                status.text = "Status: ${kunjunganItem.status ?: "Tidak tersedia"}"
                ket.text = "Keterangan: ${kunjunganItem.keterangan ?: "Tidak tersedia"}"
            }
        }
    }

    override fun getItemCount(): Int = if (isLoading) 5 else kunjungan.size

    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            notifyDataSetChanged()
        }
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Tidak tersedia"
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Format tidak valid"
        }
    }
    private fun formatTime(timestamp: Long?): String {
        if (timestamp == null) return "Tidak tersedia"
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Format tidak valid"
        }
    }
}