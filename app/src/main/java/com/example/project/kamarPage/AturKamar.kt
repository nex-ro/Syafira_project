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
import com.example.project.R
import com.example.project.databinding.FragmentAturKamarBinding
import com.google.firebase.database.*

class AturKamar : Fragment() {

    private lateinit var jenisPasienSpinner: Spinner
    private lateinit var kamarSpinner: Spinner
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentAturKamarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAturKamarBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance().reference.child("Ruangan")

        // Initialize Spinners
        jenisPasienSpinner = binding.JenisPasien
        kamarSpinner = binding.SpinnerKamar

        setupJenisPasienSpinner()
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

                    if (excludeIcuHcu) {
                        // Include all except ICU and HCU for Rawat Jalan and Rawat Inap
                        if (jenisRuangan != "ICU" && jenisRuangan != "HCU") {
                            kamarList.add(namaRuangan)
                        }
                    } else {
                        // Include only ICU and HCU for Rawat Darurat
                        if (jenisRuangan == "ICU" || jenisRuangan == "HCU") {
                            kamarList.add(namaRuangan)
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

        // Reconnect the EditText below Spinner Kamar to Spinner JenisPasien directly
        constraintSet.connect(
            R.id.editTextIdRuangan,
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

        // Reconnect the EditText below Spinner Kamar to Spinner Kamar
        constraintSet.connect(
            R.id.editTextIdRuangan,
            ConstraintSet.TOP,
            R.id.SpinnerKamar,
            ConstraintSet.BOTTOM,
            16
        )
        constraintSet.applyTo(constraintLayout)
    }
}
