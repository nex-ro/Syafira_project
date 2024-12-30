package com.example.project.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.DateItem
import com.example.project.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.content.ContextCompat

class TanggalAdapter(
    private val dates: List<DateItem>,
    private val onDateClick: (Int) -> Unit
) : RecyclerView.Adapter<TanggalAdapter.TanggalViewHolder>() {

    private var selectedPosition = 0

    inner class TanggalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tanggal: TextView = itemView.findViewById(R.id.tgl)
        val bulan: TextView = itemView.findViewById(R.id.bulan)
        val tahun: TextView = itemView.findViewById(R.id.tahun)
        val cardView: CardView = itemView.findViewById(R.id.bigCard)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    updateSelectedPosition(position)
                    onDateClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TanggalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_tanggal, parent, false)
        return TanggalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TanggalViewHolder, position: Int) {
        val dateItem = dates[position]
        val calendar = Calendar.getInstance().apply {
            time = dateItem.date
        }

        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale("id"))
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        // Set the text values
        holder.tanggal.text = dateFormat.format(dateItem.date)
        holder.bulan.text = monthFormat.format(dateItem.date)
        holder.tahun.text = yearFormat.format(dateItem.date)

        // Handle selection state
        if (position == selectedPosition) {
            // Selected state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.ungu))
            holder.tanggal.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.bulan.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.tahun.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        } else {
            // Unselected state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.tanggal.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            holder.bulan.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            holder.tahun.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }
    }

    override fun getItemCount() = dates.size

    fun updateSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(position)
    }
}
