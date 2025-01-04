package com.example.project.kamar_admin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.project.Data.Pasien
import com.example.project.databinding.FragmentPindahKamarBinding
import com.google.firebase.database.*
import kotlin.Exception

class PindahKamar(private val pasien: Pasien) : DialogFragment() {
    private var _binding: FragmentPindahKamarBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var ref_pasien: DatabaseReference
    private var isUpdating = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPindahKamarBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        try {
            setupDatabase()
            setupViews()
            loadKamarData()
        } catch (e: Exception) {
            showToast("Terjadi kesalahan: ${e.message}")
            dismiss()
        }

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
    }

    private fun setupViews() {
        // Set initial values
        binding.apply {
            textNamaPasien.text = pasien.nama_Pasien
            textPenyakit.text = pasien.penyakit
            textKamarSekarang.text = pasien.nama_Ruangan

            buttonSubmit.setOnClickListener {
                if (!isUpdating) {
                    handleSubmit()
                }
            }

            buttonBatal.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun loadKamarData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kamarList = mutableListOf<String>()

                for (roomSnapshot in snapshot.children) {
                    try {
                        val namaRuangan = roomSnapshot.child("nama_Ruangan").getValue(String::class.java) ?: continue
                        val statusRuangan = roomSnapshot.child("status").getValue(String::class.java) ?: continue
                        val jenisRuangan = roomSnapshot.child("jenis").getValue(String::class.java) ?: continue

                        if (statusRuangan != "penuh" && namaRuangan != pasien.nama_Ruangan) {
                            when (pasien.status) {
                                "Rawat Darurat" -> {
                                    if (jenisRuangan == "ICU" || jenisRuangan == "HCU") {
                                        kamarList.add(namaRuangan)
                                    }
                                }
                                "Rawat Inap" -> {
                                    if (jenisRuangan != "ICU" && jenisRuangan != "HCU") {
                                        kamarList.add(namaRuangan)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }

                updateSpinnerVisibility(kamarList)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Gagal memuat data: ${error.message}")
            }
        })
    }

    private fun updateSpinnerVisibility(kamarList: List<String>) {
        binding.apply {
            if (kamarList.isEmpty()) {
                spinnerKamarBaru.visibility = View.GONE
                buttonSubmit.isEnabled = false
                showToast("Tidak ada kamar yang tersedia untuk jenis perawatan ini")
            } else {
                spinnerKamarBaru.visibility = View.VISIBLE
                buttonSubmit.isEnabled = true
                setupKamarSpinner(kamarList)
            }
        }
    }

    private fun setupKamarSpinner(kamarList: List<String>) {
        context?.let { ctx ->
            val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, kamarList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerKamarBaru.adapter = adapter
        }
    }

    private fun handleSubmit() {
        val kamarBaru = binding.spinnerKamarBaru.selectedItem?.toString()

        if (kamarBaru == null) {
            showToast("Pilih kamar baru terlebih dahulu")
            return
        }

        isUpdating = true
        binding.buttonSubmit.isEnabled = false

        // Use transaction to ensure atomic updates
        val updates = HashMap<String, Any>()

        // First, get all the necessary data
        database.orderByChild("nama_Ruangan").equalTo(pasien.nama_Ruangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(oldRoomSnapshot: DataSnapshot) {
                    database.orderByChild("nama_Ruangan").equalTo(kamarBaru)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(newRoomSnapshot: DataSnapshot) {
                                try {
                                    performUpdates(oldRoomSnapshot, newRoomSnapshot, kamarBaru, updates)
                                } catch (e: Exception) {
                                    isUpdating = false
                                    binding.buttonSubmit.isEnabled = true
                                    showToast("Gagal memperbarui data: ${e.message}")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                isUpdating = false
                                binding.buttonSubmit.isEnabled = true
                                showToast("Gagal memuat data kamar baru: ${error.message}")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    isUpdating = false
                    binding.buttonSubmit.isEnabled = true
                    showToast("Gagal memuat data kamar lama: ${error.message}")
                }
            })
    }

    private fun performUpdates(oldRoomSnapshot: DataSnapshot, newRoomSnapshot: DataSnapshot,
                               kamarBaru: String, updates: HashMap<String, Any>) {
        // Update old room
        val oldRoomKey = oldRoomSnapshot.children.firstOrNull()?.key
        oldRoomKey?.let { key ->
            val currentIsi = oldRoomSnapshot.children.first().child("isi").getValue(Int::class.java) ?: 0
            val updatedIsi = maxOf(0, currentIsi - 1)
            updates["Ruangan/$key/isi"] = updatedIsi
            updates["Ruangan/$key/status"] = if (updatedIsi == 0) "kosong" else "terisi"
        }

        // Update new room
        val newRoomKey = newRoomSnapshot.children.firstOrNull()?.key
        newRoomKey?.let { key ->
            val currentIsi = newRoomSnapshot.children.first().child("isi").getValue(Int::class.java) ?: 0
            val kapasitas = newRoomSnapshot.children.first().child("kapasitas").getValue(Int::class.java) ?: 0
            val updatedIsi = currentIsi + 1
            updates["Ruangan/$key/isi"] = updatedIsi
            updates["Ruangan/$key/status"] = if (updatedIsi >= kapasitas) "penuh" else "terisi"
        }

        // Update patient data
        ref_pasien.orderByChild("nama_Pasien").equalTo(pasien.nama_Pasien)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val patientKey = snapshot.children.firstOrNull()?.key
                    patientKey?.let { key ->
                        updates["pasien/$key/nama_Ruangan"] = kamarBaru

                        // Perform all updates in a single transaction
                        FirebaseDatabase.getInstance().reference.updateChildren(updates)
                            .addOnSuccessListener {
                                showToast("Pasien berhasil dipindahkan")
                                dismiss()
                            }
                            .addOnFailureListener { e ->
                                isUpdating = false
                                binding.buttonSubmit.isEnabled = true
                                showToast("Gagal memperbarui data: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    isUpdating = false
                    binding.buttonSubmit.isEnabled = true
                    showToast("Gagal memperbarui data pasien: ${error.message}")
                }
            })
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}