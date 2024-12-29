package com.example.project.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Pasien
import com.example.project.databinding.CardCariPasienBinding

class PatientAdapter(
    private var patientList: MutableList<Pasien>,
    private val onItemClick: (Pasien) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(private val binding: CardCariPasienBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Pasien) {
            binding.namaPasien.text = patient.nama_Pasien
            binding.ruangPasien.text = patient.nama_Ruangan

            itemView.setOnClickListener {
                onItemClick(patient)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = CardCariPasienBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patientList[position])
    }

    override fun getItemCount() = patientList.size

    fun updateData(newList: List<Pasien>) {
        patientList.clear()
        patientList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filterList(query: String) {
        val filteredList = patientList.filter { patient ->
            patient.nama_Pasien?.contains(query, ignoreCase = true) == true
        }
        updateData(filteredList)
    }
}