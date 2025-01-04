package com.example.project.statistikPage


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.WaktuTungguDisplay
import com.example.project.R
class WaktuTungguAdapter(private val waktuTungguList: List<WaktuTungguDisplay>) :
    RecyclerView.Adapter<WaktuTungguAdapter.WaktuTungguViewHolder>() {

    class WaktuTungguViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jenisRuangan: TextView = itemView.findViewById(R.id.tvJenisRuangan)
        val waktuTunggu: TextView = itemView.findViewById(R.id.tvWaktuTunggu)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val nomorAntrian: TextView = itemView.findViewById(R.id.tvNomorAntrian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaktuTungguViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waktu_tunggu, parent, false)
        return WaktuTungguViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaktuTungguViewHolder, position: Int) {
        val item = waktuTungguList[position]
        holder.jenisRuangan.text = item.jenisRuangan
        holder.waktuTunggu.text = "${item.waktuTunggu}"
        holder.status.text = item.status
        holder.nomorAntrian.text = item.nomorAntrian
    }

    override fun getItemCount(): Int = waktuTungguList.size
}

