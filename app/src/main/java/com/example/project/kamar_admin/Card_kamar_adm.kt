package com.example.project.kamar_admin
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Ruangan
import com.example.project.Data.cardData
import com.example.project.R

class Card_kamar_adm(
    private var mList: List<Ruangan>,
    private val fragment: androidx.fragment.app.Fragment
) : RecyclerView.Adapter<Card_kamar_adm.ViewHolder>() {

    fun updateData(newList: List<Ruangan>) {
        mList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_kamar_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            KamarDetailModal.newInstance(item).show(fragment.parentFragmentManager, "KamarDetailModal")
        }
    }

    override fun getItemCount(): Int = mList.size

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        private val jeniskamar: TextView = itemView.findViewById(R.id.jenisKamar)
        private val nomorKamar: TextView = itemView.findViewById(R.id.nomorKamar)

        fun bind(item: Ruangan) {
            jeniskamar.text = item.jenis
            nomorKamar.text = item.nomor_Ruangan.toString()
        }
    }
}
