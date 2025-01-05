package com.example.project.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Pasien
import com.example.project.Pop_up
import com.example.project.databinding.CardCariPasienBinding
import android.content.SharedPreferences

class PatientAdapter(
    private val patientList: MutableList<Pasien>,
    private val sharedPreferences: SharedPreferences,
    private val showPatientDetail: (Pasien) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(private val binding: CardCariPasienBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Pasien) {
            with(binding) {
                namaPasien.text = patient.nama_Pasien
                ruangPasien.text = patient.nama_Ruangan

                root.setOnClickListener {
                    val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
                    if (isLoggedIn) {
                        showPatientDetail(patient) // Callback untuk menampilkan halaman detail
                    } else {
                        val context = itemView.context
                        if (context is FragmentActivity) {
                            val customDialog = Pop_up(context)
                            customDialog.show()
                        }
                    }
                }
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
}

