package com.example.project.user

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.databinding.FragmentUserFormKunjunganBinding
import com.example.project.Data.Ruangan
import com.example.project.Data.Pasien
import com.example.project.Data.Kunjungan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class user_form_Kunjungan : Fragment() {
    private var _binding: FragmentUserFormKunjunganBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val database = FirebaseDatabase.getInstance()
    private val ruanganRef = database.getReference("Ruangan")
    private val pasienRef = database.getReference("pasien")
    private val kunjunganRef = database.getReference("Kunjungan")
    private var selectedDate: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserFormKunjunganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePicker()
        loadRuangan()
        setupSpinners()
        setupSubmitButton()
    }

    private fun setupSpinners() {
        binding.dropdownKamarPasien.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRuanganName = parent?.getItemAtPosition(position).toString()
                loadPasienForRuangan(selectedRuanganName)
                updateJamKunjunganOptions(selectedRuanganName)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadRuangan() {
        ruanganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ruanganList = mutableListOf<String>()
                for (ruanganSnapshot in snapshot.children) {
                    val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                    ruangan?.nama_Ruangan?.let {
                        if (!ruanganList.contains(it)) {
                            ruanganList.add(it)
                        }
                    }
                }

                if (ruanganList.isNotEmpty()) {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        ruanganList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dropdownKamarPasien.adapter = adapter

                    // Load patients for first room automatically
                    loadPasienForRuangan(ruanganList[0])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load rooms: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPasienForRuangan(ruanganName: String) {
        pasienRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pasienList = mutableListOf<String>()
                for (pasienSnapshot in snapshot.children) {
                    val pasien = pasienSnapshot.getValue(Pasien::class.java)
                    if (pasien?.nama_Ruangan == ruanganName && pasien.nama_Pasien != null) {
                        pasienList.add(pasien.nama_Pasien)
                    }
                }

                if (pasienList.isNotEmpty()) {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        pasienList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dropdownNamaPasien.adapter = adapter
                } else {
                    // If no patients found, show empty adapter
                    binding.dropdownNamaPasien.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        listOf("No patients available")
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load patients: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateJamKunjunganOptions(ruanganName: String) {
        ruanganRef.orderByChild("nama_Ruangan").equalTo(ruanganName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ruangan = snapshot.children.firstOrNull()?.getValue(Ruangan::class.java)
                    val jamKunjunganList = if (ruangan?.jenis in listOf("ICU", "HCU")) {
                        listOf(
                            "11:00 - 12:00",
                            "17:00 - 18:00"
                        )
                    } else {
                        listOf(
                            "10:00 - 14:00",
                            "19:00 - 21:00"
                        )
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        jamKunjunganList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.dropdownJamKunjungan.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to update visit times: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupDatePicker() {
        binding.inputTanggalKunjungan.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePickerDialog() }
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialogTheme,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.timeInMillis
                binding.inputTanggalKunjungan.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            val maxDate = Calendar.getInstance()
            maxDate.add(Calendar.DAY_OF_MONTH, 30)
            datePicker.maxDate = maxDate.timeInMillis
            show()
        }
    }

    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            val nama = binding.inputNama.text.toString()
            val kamarPasien = binding.dropdownKamarPasien.selectedItem?.toString()
            val namaPasien = binding.dropdownNamaPasien.selectedItem?.toString()
            val jamKunjungan = binding.dropdownJamKunjungan.selectedItem?.toString()
            val hubungan = binding.inputHubungan.text.toString()
            val userId = getUserId()

            if (validateInput(nama, kamarPasien, namaPasien, selectedDate, jamKunjungan, hubungan)) {
                val kunjungan = Kunjungan(
                    id_pengunjung = userId,
                    nama = nama,
                    kamar_pasien = kamarPasien,
                    nama_pasien = namaPasien,
                    tanggal_kunjungan = selectedDate.toInt(),
                    jam_kunjungan = convertJamKunjunganToInt(jamKunjungan ?: ""),
                    hubungan = hubungan
                )

                submitKunjungan(kunjungan)
            }
        }
    }

    private fun validateInput(
        nama: String,
        kamarPasien: String?,
        namaPasien: String?,
        tanggalKunjungan: Long,
        jamKunjungan: String?,
        hubungan: String
    ): Boolean {
        when {
            nama.isEmpty() -> {
                Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            kamarPasien == null || kamarPasien == "No rooms available" -> {
                Toast.makeText(context, "Pilih kamar pasien", Toast.LENGTH_SHORT).show()
                return false
            }
            namaPasien == null || namaPasien == "No patients available" -> {
                Toast.makeText(context, "Pilih nama pasien", Toast.LENGTH_SHORT).show()
                return false
            }
            tanggalKunjungan == 0L -> {
                Toast.makeText(context, "Pilih tanggal kunjungan", Toast.LENGTH_SHORT).show()
                return false
            }
            jamKunjungan == null -> {
                Toast.makeText(context, "Pilih jam kunjungan", Toast.LENGTH_SHORT).show()
                return false
            }
            hubungan.isEmpty() -> {
                Toast.makeText(context, "Hubungan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun convertJamKunjunganToInt(jamKunjungan: String): Int {
        return when (jamKunjungan) {
            "11:00 - 12:00" -> 1100
            "17:00 - 18:00" -> 1700
            "10:00 - 14:00" -> 1000
            "19:00 - 21:00" -> 1900
            else -> 0
        }
    }

    private fun submitKunjungan(kunjungan: Kunjungan) {
        val newKunjunganRef = kunjunganRef.push()
        newKunjunganRef.setValue(kunjungan)
            .addOnSuccessListener {
                Toast.makeText(context, "Kunjungan berhasil disubmit", Toast.LENGTH_SHORT).show()
                clearForm()
                setCurrentFragment(user_home())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal submit kunjungan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        binding.inputNama.text?.clear()
        binding.inputTanggalKunjungan.text?.clear()
        binding.inputHubungan.text?.clear()
        selectedDate = 0
        loadRuangan() // Reset spinners
    }

    private fun getUserId(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("id", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}