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
        val jenisList = listOf("VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium","ICU","HCU")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter

        binding.buttonSimpan.setOnClickListener(){
            if(binding.inputNamaRuangan.text.isEmpty() and binding.inputNomorRuangan.text.isEmpty()){
                Toast.makeText(
                    requireContext(),
                    "Mohon Input dengan benar",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                val nama=binding.inputNamaRuangan.text.toString()
                val nomor=binding.inputNomorRuangan.text.toString().toIntOrNull()
                val jenis=binding.spinnerJenis.selectedItem.toString()
                val status="kosong"
                val id_ruangan = ref.push().key ?: return@setOnClickListener
                val ruangan=Ruangan(id_ruangan,nomor,jenis,nama,status)
                ref.child(id_ruangan).setValue(ruangan).addOnCompleteListener{task ->
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
        return binding.root
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

}