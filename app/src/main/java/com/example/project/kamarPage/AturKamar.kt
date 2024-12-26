package com.example.project.kamarPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.example.project.Data.History
import com.example.project.Data.Pasien
import com.example.project.Data.Ruangan
import com.example.project.R
import com.example.project.databinding.FragmentAturKamarBinding
import com.example.project.kamar_adm
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AturKamar : Fragment() {

    private lateinit var jenisPasienSpinner: Spinner
    private lateinit var kamarSpinner: Spinner
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentAturKamarBinding
    private lateinit var ref_pasien: DatabaseReference
    private lateinit var ref_History: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAturKamarBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference.child("Ruangan")
        ref_pasien=FirebaseDatabase.getInstance().reference.child("pasien")
        ref_History=FirebaseDatabase.getInstance().reference.child("history")
        // Initialize Spinners
        jenisPasienSpinner = binding.JenisPasien
        kamarSpinner = binding.SpinnerKamar

        setupJenisPasienSpinner()
        binding.buttonSubmitt.setOnClickListener{
            val nama=binding.editTextNamaPasien.text.toString()
            val jenis=binding.JenisPasien.selectedItem.toString()
            val penyakit=binding.editTextPenyakit.text.toString()
            val waktu =System.currentTimeMillis().toLong()

            if(jenis=="Rawat Jalan"){
                val idHistory=ref_History.push().key ?: ""
                val historyy=History(nama,penyakit,waktu,waktu)
                ref_History.child(idHistory).setValue(historyy).addOnCompleteListener { task ->
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

                }else{
                val idPasien = ref_pasien.push().key ?: ""
                val kamar=binding.SpinnerKamar.selectedItem.toString()
                val pasienn = Pasien(nama,penyakit,kamar,waktu,jenis)
                ref_pasien.child(idPasien).setValue(pasienn).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateRuanganStatus(kamar)
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

    private fun setupJenisPasienSpinner() {
        val jenisOptions = listOf("Rawat Jalan", "Rawat Darurat", "Rawat Inap")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisPasienSpinner.adapter = adapter

        jenisPasienSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedJenis = jenisOptions[position]

                if (selectedJenis == "Rawat Jalan") {
                    kamarSpinner.visibility = View.GONE
                    adjustConstraintsForHiddenKamarSpinner()
                } else {
                    kamarSpinner.visibility = View.VISIBLE
                    adjustConstraintsForVisibleKamarSpinner()
                    when (selectedJenis) {
                        "Rawat Inap" -> loadKamarData(selectedJenis, excludeIcuHcu = true)
                        "Rawat Darurat" -> loadKamarData(selectedJenis, excludeIcuHcu = false)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadKamarData(selectedJenis: String, excludeIcuHcu: Boolean) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kamarList = mutableListOf<String>()

                for (roomSnapshot in snapshot.children) {
                    val namaRuangan = roomSnapshot.child("nama_Ruangan").value.toString()
                    val jenisRuangan = roomSnapshot.child("jenis").value.toString()
                    val statusRuangan = roomSnapshot.child("status").value.toString()

                    if (statusRuangan != "penuh") {
                        if (excludeIcuHcu) {
                            if (jenisRuangan != "ICU" && jenisRuangan != "HCU") {
                                kamarList.add(namaRuangan)
                            }
                        } else {
                            if (jenisRuangan == "ICU" || jenisRuangan == "HCU") {
                                kamarList.add(namaRuangan)
                            }
                        }
                    }
                }
                setupKamarSpinner(kamarList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupKamarSpinner(kamarList: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kamarList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        kamarSpinner.adapter = adapter
    }

    private fun adjustConstraintsForHiddenKamarSpinner() {
        val constraintLayout = binding.root as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.connect(
            R.id.buttonSubmitt,
            ConstraintSet.TOP,
            R.id.JenisPasien,
            ConstraintSet.BOTTOM,
            16
        )
        constraintSet.applyTo(constraintLayout)
    }

    private fun adjustConstraintsForVisibleKamarSpinner() {
        val constraintLayout = binding.root as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.connect(
            R.id.buttonSubmitt,
            ConstraintSet.TOP,
            R.id.SpinnerKamar,
            ConstraintSet.BOTTOM,
            16
        )
        constraintSet.applyTo(constraintLayout)
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null) // Tambahkan ke backstack
            commit()
        }

    private fun updateRuanganStatus(namaRuangan: String) {
        database.orderByChild("nama_Ruangan").equalTo(namaRuangan).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (roomSnapshot in snapshot.children) {
                    val currentIsi = roomSnapshot.child("isi").getValue(Int::class.java) ?: 0
                    val kapasitas = roomSnapshot.child("kapasitas").getValue(Int::class.java) ?: 0

                    val updatedIsi = currentIsi + 1
                    val updatedStatus = if (updatedIsi >= kapasitas) "penuh" else "terisi"

                    roomSnapshot.ref.child("isi").setValue(updatedIsi)
                    roomSnapshot.ref.child("status").setValue(updatedStatus)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memperbarui status ruangan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
