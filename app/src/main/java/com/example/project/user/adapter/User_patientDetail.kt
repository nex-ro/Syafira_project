package com.example.project.user.adapter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.project.databinding.FragmentUserPatientDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

class User_patientDetail : DialogFragment() {

    private var _binding: FragmentUserPatientDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the binding
        _binding = FragmentUserPatientDetailBinding.inflate(LayoutInflater.from(context))

        // Retrieve data from arguments
        val patientName = arguments?.getString(ARG_PATIENT_NAME) ?: "N/A"
        val disease = arguments?.getString(ARG_DISEASE) ?: "N/A"
        val roomName = arguments?.getString(ARG_ROOM_NAME) ?: "N/A"
        val admissionDate = arguments?.getLong(ARG_ADMISSION_DATE, 0L) ?: 0L
        val status = arguments?.getString(ARG_STATUS) ?: "N/A"

        // Format admission date if provided
        val formattedDate = if (admissionDate != 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(admissionDate)
        } else {
            "N/A"
        }

        // Bind data to the UI
        with(binding) {
            tvPatientName.text ="Nama Pasien :"+ patientName
            tvDisease.text = "Penyakit :"+disease
            tvRoomName.text = "Nama Ruangan Pasien :"+roomName
            tvAdmissionDate.text = "Tanggal masuk :"+formattedDate
            tvStatus.text = "Status Pasien :"+status
        }

        // Return a Dialog with the bound layout
        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            setCancelable(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    companion object {
        private const val ARG_PATIENT_NAME = "nama_pasien"
        private const val ARG_DISEASE = "penyakit"
        private const val ARG_ROOM_NAME = "nama_ruangan"
        private const val ARG_ADMISSION_DATE = "tanggal_masuk"
        private const val ARG_STATUS = "status"

        /**
         * Factory method to create a new instance of this dialog with the required arguments.
         */
        fun newInstance(
            namaPasien: String?,
            penyakit: String?,
            namaRuangan: String?,
            tanggalMasuk: Long?,
            status: String?
        ): User_patientDetail {
            return User_patientDetail().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATIENT_NAME, namaPasien)
                    putString(ARG_DISEASE, penyakit)
                    putString(ARG_ROOM_NAME, namaRuangan)
                    putLong(ARG_ADMISSION_DATE, tanggalMasuk ?: 0L)
                    putString(ARG_STATUS, status)
                }
            }
        }
    }
}

