package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Ruangan
import android.util.Log
import kotlin.math.log

class jenisAdapter(
    private var mList: List<Ruangan>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<jenisAdapter.ViewHolder>() {

    fun updateData(newList: List<Ruangan>) {
        mList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_card_jenis, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.namaRuangan.text = item.nama_Ruangan
        holder.nomorRuangan.text = "Nomor : ${item.nomor_Ruangan}"
        holder.statusRuangan.text = "Status : ${item.status}"

        holder.itemView.setOnClickListener {
            item.id_Ruangan?.let { id ->
                onItemClick(id)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val namaRuangan: TextView = itemView.findViewById(R.id.namaRuangan)
        val nomorRuangan: TextView = itemView.findViewById(R.id.nomorRuangan)
        val statusRuangan: TextView = itemView.findViewById(R.id.statusRuangan)
    }
}
