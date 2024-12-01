package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Ruangan
import com.example.project.Data.cardData

class ItemAdapter(
    private var mList: List<cardData>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    fun updateData(newList: List<cardData>) {
        mList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.jeniskamar.text = item.jenis
        holder.empty.text = "Kosong: ${item.kosong}"
        holder.terisi.text = "Terisi: ${item.terisi}"

        holder.itemView.setOnClickListener {
            onItemClick(item.jenis ?: "")
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val jeniskamar: TextView = itemView.findViewById(R.id.jeniskamar)
        val empty: TextView = itemView.findViewById(R.id.emptyRoom)
        val terisi: TextView = itemView.findViewById(R.id.tempatTidur)
    }
}
