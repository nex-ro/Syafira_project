package com.example.project.kamarPage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.project.Data.History
import com.example.project.Data.Pasien
import com.example.project.databinding.FragmentAturKamarBinding
import com.google.firebase.database.*

class AturKamar : DialogFragment() {
    private var _binding: FragmentAturKamarBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var ref_pasien: DatabaseReference
    private lateinit var ref_History: DatabaseReference

    private var isSubmitting = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAturKamarBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setupDatabase()
        setupViews()
        setupJenisPasienSpinner()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    private fun setupDatabase() {
        database = FirebaseDatabase.getInstance().reference.child("Ruangan")
        ref_pasien = FirebaseDatabase.getInstance().reference.child("pasien")
        ref_History = FirebaseDatabase.getInstance().reference.child("history")
    }

    private fun setupViews() {
        binding.buttonSubmit.setOnClickListener {
            if (!isSubmitting) {
                handleSubmit()
            }
        }

        binding.buttonBatal.setOnClickListener {
            dismiss()
        }
    }

    private fun setupJenisPasienSpinner() {
        val jenisOptions = listOf("Rawat Jalan", "Rawat Darurat", "Rawat Inap")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenisPasien.adapter = adapter

        binding.spinnerJenisPasien.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedJenis = jenisOptions[position]
                handleJenisPasienSelection(selectedJenis)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun handleJenisPasienSelection(selectedJenis: String) {
        binding.spinnerKamar.visibility = if (selectedJenis == "Rawat Jalan") View.GONE else View.VISIBLE

        when (selectedJenis) {
            "Rawat Inap" -> loadKamarData(excludeIcuHcu = true)
            "Rawat Darurat" -> loadKamarData(excludeIcuHcu = false)
        }
    }

    private fun loadKamarData(excludeIcuHcu: Boolean) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kamarList = mutableListOf<String>()

                for (roomSnapshot in snapshot.children) {
                    val namaRuangan = roomSnapshot.child("nama_Ruangan").value?.toString() ?: continue
                    val jenisRuangan = roomSnapshot.child("jenis").value?.toString() ?: continue
                    val statusRuangan = roomSnapshot.child("status").value?.toString() ?: continue

                    if (statusRuangan != "penuh") {
                        if (excludeIcuHcu && jenisRuangan != "ICU" && jenisRuangan != "HCU") {
                            kamarList.add(namaRuangan)
                        } else if (!excludeIcuHcu && (jenisRuangan == "ICU" || jenisRuangan == "HCU")) {
                            kamarList.add(namaRuangan)
                        }
                    }
                }

                if (kamarList.isEmpty()) {
                    showError("Tidak ada kamar yang tersedia")
                } else {
                    setupKamarSpinner(kamarList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Gagal memuat data kamar: ${error.message}")
            }
        })
    }

    private fun setupKamarSpinner(kamarList: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kamarList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKamar.adapter = adapter
    }

    private fun handleSubmit() {
        isSubmitting = true

        val nama = binding.editTextNamaPasien.text?.toString()?.trim() ?: ""
        val jenis = binding.spinnerJenisPasien.selectedItem?.toString() ?: ""
        val penyakit = binding.editTextPenyakit.text?.toString()?.trim() ?: ""
        val waktu = System.currentTimeMillis()

        if (nama.isEmpty() || penyakit.isEmpty()) {
            showError("Mohon isi semua data")
            isSubmitting = false
            return
        }

        when (jenis) {
            "Rawat Jalan" -> submitRawatJalan(nama, penyakit, waktu)
            "Rawat Darurat", "Rawat Inap" -> {
                val kamar = binding.spinnerKamar.selectedItem?.toString()
                if (kamar == null) {
                    showError("Pilih kamar terlebih dahulu")
                    isSubmitting = false
                    return
                }
                submitRawatInapDarurat(nama, penyakit, jenis, kamar, waktu)
            }
        }
    }

    private fun submitRawatJalan(nama: String, penyakit: String, waktu: Long) {
        val idHistory = ref_History.push().key ?: return
        val history = History(nama, penyakit, waktu, waktu)

        ref_History.child(idHistory).setValue(history)
            .addOnSuccessListener {
                handleSubmitResult(true)
            }
            .addOnFailureListener {
                handleSubmitResult(false)
            }
    }

    private fun submitRawatInapDarurat(nama: String, penyakit: String, jenis: String, kamar: String, waktu: Long) {
        val idPasien = ref_pasien.push().key ?: return
        val pasien = Pasien(nama, penyakit, kamar, waktu, jenis)

        ref_pasien.child(idPasien).setValue(pasien)
            .addOnSuccessListener {
                updateRuanganStatus(kamar, idPasien)
            }
            .addOnFailureListener {
                handleSubmitResult(false)
            }
    }

    private fun updateRuanganStatus(namaRuangan: String, idPasien: String) {
        database.orderByChild("nama_Ruangan").equalTo(namaRuangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (roomSnapshot in snapshot.children) {
                        val currentIsi = roomSnapshot.child("isi").getValue(Int::class.java) ?: 0
                        val kapasitas = roomSnapshot.child("kapasitas").getValue(Int::class.java) ?: 1

                        if (currentIsi + 1 > kapasitas) {
                            ref_pasien.child(idPasien).removeValue()
                            showError("Kamar penuh, data dibatalkan")
                            isSubmitting = false
                            return
                        }

                        val updates = mapOf(
                            "isi" to currentIsi + 1,
                            "status" to if (currentIsi + 1 == kapasitas) "penuh" else "terisi"
                        )

                        roomSnapshot.ref.updateChildren(updates)
                            .addOnSuccessListener {
                                handleSubmitResult(true)
                            }
                            .addOnFailureListener {
                                handleSubmitResult(false)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    ref_pasien.child(idPasien).removeValue()
                    showError("Gagal memperbarui status kamar: ${error.message}")
                }
            })
    }

    private fun handleSubmitResult(isSuccessful: Boolean) {
        if (isSuccessful) {
            showSuccess("Data berhasil ditambahkan")
            dismiss()
        } else {
            showError("Gagal menambahkan data")
        }
        isSubmitting = false
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
