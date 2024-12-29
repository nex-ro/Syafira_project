package com.example.project.user.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Kunjungan
import com.example.project.databinding.CardKunjunganBinding
import java.text.SimpleDateFormat
import java.util.*

class KunjunganAdapter : RecyclerView.Adapter<KunjunganAdapter.KunjunganViewHolder>() {
    private var kunjunganList = mutableListOf<Kunjungan>()

    class KunjunganViewHolder(private val binding: CardKunjunganBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(kunjungan: Kunjungan) {
            val date = Date(kunjungan.tanggal_kunjungan ?: 0)
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val fullDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

            // Set date card texts
            binding.apply {
                root.findViewById<android.widget.TextView>(com.example.project.R.id.tgl).text = dayFormat.format(date)
                root.findViewById<android.widget.TextView>(com.example.project.R.id.bulan).text = monthFormat.format(date)
                root.findViewById<android.widget.TextView>(com.example.project.R.id.tahun).text = yearFormat.format(date)

                // Set visit information
                root.findViewById<android.widget.TextView>(com.example.project.R.id.pasien).text =
                    "Pasien dikunjungi : ${kunjungan.nama_pasien}"
                root.findViewById<android.widget.TextView>(com.example.project.R.id.ruangan).text =
                    "Ruangan : ${kunjungan.kamar_pasien}"
                root.findViewById<android.widget.TextView>(com.example.project.R.id.keterangan).text =
                    "Ruangan : ${kunjungan.keterangan}"
                root.findViewById<android.widget.TextView>(com.example.project.R.id.waktukedatangan).text =
                    "Waktu Kunjungan : ${kunjungan.jam_kunjungan}"


                // Set status with color
                val statusText = root.findViewById<android.widget.TextView>(com.example.project.R.id.textstatus)
                statusText.text = kunjungan.status

                when (kunjungan.status?.lowercase()) {
                    "menunggu" -> statusText.setTextColor(Color.parseColor("#FFA500")) // Yellow
                    "ditolak" -> statusText.setTextColor(Color.RED)
                    "diterima", "selesai" -> statusText.setTextColor(Color.parseColor("#008000")) // Green
                }
            }
        }
    }

    fun updateData(newList: List<Kunjungan>) {
        kunjunganList.clear()
        kunjunganList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KunjunganViewHolder {
        val binding = CardKunjunganBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KunjunganViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KunjunganViewHolder, position: Int) {
        holder.bind(kunjunganList[position])
    }

    override fun getItemCount() = kunjunganList.size
}
