package com.example.project.kamar_admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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

        binding.editTextNamaPasien.isEnabled = false
        binding.editTextPenyakit.hint = "Penyakit (${pasien.penyakit})"
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Rawat Jalan", "Rawat Darurat", "Rawat Inap")
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions.map { "$it ${if (it == pasien.status) "(Kondisi Saat Ini)" else ""}" }
        )
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
                    "Rawat Jalan" -> {
                        binding.spinnerKamar.visibility = View.GONE
                        if (pasien.status != "Rawat Jalan") {
                            handleStatusChangeToRawatJalan()
                        }
                    }
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
                Toast.makeText(requireContext(), "Gagal memuat ruangan: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun handleStatusChangeToRawatJalan() {
        database.child("pasien")
            .orderByChild("nama_Pasien")
            .equalTo(pasien.nama_Pasien)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val patientKey = snapshot.children.firstOrNull()?.key
                    if (patientKey != null) {
                        moveToHistory(patientKey)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal mengubah status: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun moveToHistory(patientKey: String) {
        val currentTime = System.currentTimeMillis()

        val historyEntry = History(
            nama = pasien.nama_Pasien,
            penyakit = binding.editTextPenyakit.text.toString(),
            tanggal_Masuk = pasien.tanggal_Masuk ?: currentTime,
            tanggal_Keluar = currentTime,
        )

        database.child("history")
            .push()
            .setValue(historyEntry)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pasien berhasil dipindahkan ke riwayat.", Toast.LENGTH_SHORT).show()
                if (pasien.nama_Ruangan != null) {
                    updateOldRoom {
                        removePatientAndFinish(patientKey)
                    }
                } else {
                    removePatientAndFinish(patientKey)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal memindahkan ke riwayat: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removePatientAndFinish(patientKey: String) {
        database.child("pasien")
            .child(patientKey)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pasien berhasil dihapus.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal menghapus pasien: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Operasi dibatalkan.", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.buttonSubmit.setOnClickListener {
            updateData()
        }
    }

    private fun updateData() {
        val selectedStatus = binding.spinnerJenisPasien.selectedItem.toString().split(" (")[0]
        val newPenyakit = binding.editTextPenyakit.text.toString()

        database.child("pasien")
            .orderByChild("nama_Pasien")
            .equalTo(pasien.nama_Pasien)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val patientKey = snapshot.children.firstOrNull()?.key
                    if (patientKey != null) {
                        when (selectedStatus) {
                            "Rawat Jalan" -> moveToHistory(patientKey)
                            else -> handleOtherStatus(patientKey, selectedStatus, newPenyakit)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memperbarui data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleOtherStatus(patientKey: String, selectedStatus: String, newPenyakit: String) {
        if (binding.spinnerKamar.visibility == View.VISIBLE && ruanganList.isEmpty()) {
            Toast.makeText(requireContext(), "Ruangan tidak tersedia untuk status ini.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>(
            "penyakit" to newPenyakit,
            "status" to selectedStatus
        )

        if (binding.spinnerKamar.visibility == View.VISIBLE) {
            val selectedRuangan = ruanganList[binding.spinnerKamar.selectedItemPosition]

            if (selectedRuangan.nama_Ruangan != pasien.nama_Ruangan) {
                updates["nama_Ruangan"] = selectedRuangan.nama_Ruangan ?: ""

                updateOldRoom {
                    updateNewRoom(selectedRuangan) {
                        database.child("pasien")
                            .child(patientKey)
                            .updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Data pasien berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                                dismiss()
                            }
                    }
                }
            } else {
                database.child("pasien")
                    .child(patientKey)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Data pasien berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
            }
        } else {
            database.child("pasien")
                .child(patientKey)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Data pasien berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
        }
    }

    private fun updateOldRoom(onComplete: () -> Unit = {}) {
        if (pasien.nama_Ruangan == null) {
            onComplete()
            return
        }

        database.child("Ruangan")
            .orderByChild("nama_Ruangan")
            .equalTo(pasien.nama_Ruangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomKey = snapshot.children.firstOrNull()?.key
                    if (roomKey != null) {
                        val ruangan = snapshot.children.first().getValue(Ruangan::class.java)
                        ruangan?.let {
                            val newIsi = (it.isi ?: 1) - 1
                            val updates = hashMapOf<String, Any>(
                                "isi" to newIsi,
                                "status" to if (newIsi == 0) "kosong" else "terisi"
                            )
                            database.child("Ruangan")
                                .child(roomKey)
                                .updateChildren(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ruangan lama berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                                    onComplete()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Ruangan lama tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memperbarui ruangan lama: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateNewRoom(newRuangan: Ruangan, onComplete: () -> Unit = {}) {
        database.child("Ruangan")
            .orderByChild("nama_Ruangan")
            .equalTo(newRuangan.nama_Ruangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomKey = snapshot.children.firstOrNull()?.key
                    if (roomKey != null) {
                        val ruangan = snapshot.children.first().getValue(Ruangan::class.java)
                        ruangan?.let {
                            val newIsi = (it.isi ?: 0) + 1
                            val updates = hashMapOf<String, Any>(
                                "isi" to newIsi,
                                "status" to if (newIsi >= (it.kapasitas ?: 1)) "penuh" else "terisi"
                            )
                            database.child("Ruangan")
                                .child(roomKey)
                                .updateChildren(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ruangan baru berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                                    onComplete()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Ruangan baru tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memperbarui ruangan baru: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
