package com.example.project.kamar_admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.project.Data.History
import com.example.project.Data.Pasien
import com.example.project.Data.Ruangan
import com.example.project.databinding.FragmentPindahKamarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PindahKamar(private val pasien: Pasien) : DialogFragment() {
    private lateinit var binding: FragmentPindahKamarBinding
    private val database = FirebaseDatabase.getInstance().reference
    private val ruanganList = mutableListOf<Ruangan>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPindahKamarBinding.inflate(inflater, container, false)

        setupDefaultValues()
        setupStatusSpinner()

        if (pasien.status != "Rawat Jalan") {
            binding.spinnerKamar.visibility = View.VISIBLE
            loadRuangan(pasien.status == "Rawat Darurat")
        } else {
            binding.spinnerKamar.visibility = View.GONE
        }

        setupButtons()

        return binding.root
    }

    private fun setupDefaultValues() {
        binding.editTextNamaPasien.setText(pasien.nama_Pasien)
        binding.editTextPenyakit.setText(pasien.penyakit)

        binding.editTextNamaPasien.isEnabled = false // Disable name editing
        binding.editTextPenyakit.hint = "Penyakit (${pasien.penyakit})"
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Rawat Jalan", "Rawat Darurat", "Rawat Inap")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            statusOptions.map { "$it ${if (it == pasien.status) "(Kondisi Saat Ini)" else ""}" })
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerJenisPasien.adapter = statusAdapter

        val currentStatusIndex = statusOptions.indexOf(pasien.status)
        if (currentStatusIndex != -1) {
            binding.spinnerJenisPasien.setSelection(currentStatusIndex)
        }

        binding.spinnerJenisPasien.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statusOptions[position]
                when (selectedStatus) {
                    "Rawat Jalan" -> binding.spinnerKamar.visibility = View.GONE
                    "Rawat Darurat" -> {
                        binding.spinnerKamar.visibility = View.VISIBLE
                        loadRuangan(isEmergency = true)
                    }
                    "Rawat Inap" -> {
                        binding.spinnerKamar.visibility = View.VISIBLE
                        loadRuangan(isEmergency = false)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadRuangan(isEmergency: Boolean) {
        val loadingAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Loading ruangan...")
        )
        loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKamar.adapter = loadingAdapter

        database.child("Ruangan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ruanganList.clear()
                snapshot.children.forEach { ruanganSnapshot ->
                    val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                    ruangan?.let {
                        if (it.status != "penuh" || it.nama_Ruangan == pasien.nama_Ruangan) {
                            if (isEmergency && (it.jenis == "HCU" || it.jenis == "ICU")) {
                                ruanganList.add(it)
                            } else if (!isEmergency && it.jenis != "HCU" && it.jenis != "ICU") {
                                ruanganList.add(it)
                            }
                        }
                    }
                }
                updateRuanganSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                val errorAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listOf("Error loading ruangan")
                )
                errorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerKamar.adapter = errorAdapter
            }
        })
    }

    private fun updateRuanganSpinner() {
        if (ruanganList.isEmpty()) {
            val emptyAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Tidak ada ruangan tersedia")
            )
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerKamar.adapter = emptyAdapter
            return
        }

        val ruanganNames = ruanganList.map { ruangan ->
            "${ruangan.nama_Ruangan}${if (ruangan.nama_Ruangan == pasien.nama_Ruangan) " (Ruangan Saat Ini)" else ""}"
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ruanganNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKamar.adapter = adapter

        val currentRoomIndex = ruanganList.indexOfFirst { it.nama_Ruangan == pasien.nama_Ruangan }
        if (currentRoomIndex != -1) {
            binding.spinnerKamar.setSelection(currentRoomIndex)
        }
    }
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }
    private fun setupButtons() {
        binding.buttonBatal.setOnClickListener {
            dismiss()
        }

        binding.buttonSubmit.setOnClickListener {
            updateData()
        }
    }

    private fun updateData() {
        val selectedStatus = binding.spinnerJenisPasien.selectedItem.toString().split(" (")[0]
        val patientRef = database.child("pasien").child(pasien.nama_Pasien ?: "")
        val newPenyakit = binding.editTextPenyakit.text.toString()

        when (selectedStatus) {
            "Rawat Jalan" -> {
                // Create history record for outpatient
                val history = History(
                    nama = pasien.nama_Pasien,
                    penyakit = newPenyakit,
                    tanggal_Masuk = pasien.tanggal_Masuk ?: 0L,
                    tanggal_Keluar = System.currentTimeMillis()
                )

                // Update the old room first
                updateOldRoom {
                    // Save to history and remove from active patients
                    database.child("history").push().setValue(history).addOnSuccessListener {
                        // Remove patient data after successfully creating history
                        patientRef.removeValue().addOnSuccessListener {
                            dismiss()
                        }
                    }
                }
            }
            else -> {
                if (binding.spinnerKamar.visibility == View.VISIBLE && ruanganList.isEmpty()) {
                    return
                }

                // Create updates map for patient data
                val updates = mutableMapOf<String, Any>(
                    "penyakit" to newPenyakit,
                    "status" to selectedStatus
                )

                // Only update room if visible and selected
                if (binding.spinnerKamar.visibility == View.VISIBLE) {
                    val selectedRuangan = ruanganList[binding.spinnerKamar.selectedItemPosition]

                    if (selectedRuangan.nama_Ruangan != pasien.nama_Ruangan) {
                        updates["nama_Ruangan"] = selectedRuangan.nama_Ruangan ?: ""

                        // Update both old and new rooms
                        updateOldRoom {
                            updateNewRoom(selectedRuangan) {
                                // Update patient data after rooms are updated
                                patientRef.updateChildren(updates).addOnSuccessListener {
                                    dismiss()
                                }
                            }
                        }
                    } else {
                        // Just update patient data if room hasn't changed
                        patientRef.updateChildren(updates).addOnSuccessListener {
                            dismiss()
                        }
                    }
                } else {
                    // Update patient data without room changes
                    patientRef.updateChildren(updates).addOnSuccessListener {
                        dismiss()
                    }
                }
            }
        }
    }
    private fun updateOldRoom(onComplete: () -> Unit = {}) {
        if (pasien.nama_Ruangan == null) {
            onComplete()
            return
        }

        database.child("ruangan")
            .orderByChild("nama_Ruangan")
            .equalTo(pasien.nama_Ruangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { ruanganSnapshot ->
                        val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                        ruangan?.let {
                            val newIsi = (it.isi ?: 1) - 1
                            val updates = hashMapOf<String, Any>(
                                "isi" to newIsi,
                                "status" to if (newIsi == 0) "kosong" else "terisi"
                            )
                            ruanganSnapshot.ref.updateChildren(updates).addOnSuccessListener {
                                onComplete()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    onComplete()
                }
            })
    }
    private fun updateNewRoom(selectedRuangan: Ruangan, onComplete: () -> Unit = {}) {
        database.child("ruangan")
            .orderByChild("nama_Ruangan")
            .equalTo(selectedRuangan.nama_Ruangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { ruanganSnapshot ->
                        val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                        ruangan?.let {
                            val newIsi = (it.isi ?: 0) + 1
                            val updates = hashMapOf<String, Any>(
                                "isi" to newIsi,
                                "status" to if (newIsi >= (it.kapasitas ?: 1)) "penuh" else "terisi"
                            )
                            ruanganSnapshot.ref.updateChildren(updates).addOnSuccessListener {
                                onComplete()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    onComplete()
                }
            })
    }}