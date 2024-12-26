package com.example.project.kamarPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.databinding.FragmentProfileBinding
import com.example.project.databinding.FragmentTambahKamarBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.project.Data.Ruangan
import com.example.project.kamar
import com.example.project.kamar_adm
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class TambahKamar : Fragment() {
    private lateinit var ref: DatabaseReference
    private lateinit var binding: FragmentTambahKamarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTambahKamarBinding.inflate(inflater, container, false)
        ref = FirebaseDatabase.getInstance().reference.child("Ruangan")

        val jenisList = listOf("VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium", "ICU", "HCU")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter

        binding.buttonSimpan.setOnClickListener {
            val nama = binding.inputNamaRuangan.text.toString().trim()
            val nomor = binding.inputNomorRuangan.text.toString().toIntOrNull()
            val kapasitas=binding.inputkapasitas.text.toString().toIntOrNull()
            val jenis = binding.spinnerJenis.selectedItem.toString()
            if (nama.isEmpty() || nomor == null || kapasitas==null) {
                Toast.makeText(
                    requireContext(),
                    "Mohon input dengan benar",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isDuplicate = false
                        for (ruanganSnapshot in snapshot.children) {
                            val existingNama = ruanganSnapshot.child("nama_Ruangan").getValue(String::class.java)
                            if (existingNama != null && existingNama.equals(nama, ignoreCase = true)) {
                                isDuplicate = true
                                break
                            }
                        }

                        if (isDuplicate) {
                            Toast.makeText(
                                requireContext(),
                                "Nama ruangan sudah ada, silakan gunakan nama lain",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val status = "kosong"
                            val idRuangan = ref.push().key ?: return
                            val ruangan = Ruangan(idRuangan, nomor, jenis, nama, status,kapasitas)
                            ref.child(idRuangan).setValue(ruangan).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Data berhasil ditambahkan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setCurrentFragment(kamar_adm())
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal menambahkan data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Gagal memeriksa data: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
        return binding.root
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
