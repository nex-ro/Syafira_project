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
import androidx.core.content.ContextCompat
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
    private var isSubmitting = false
    private var isViewActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            _binding = FragmentUserFormKunjunganBinding.inflate(inflater, container, false)
            binding.root
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewActive = true

        try {
            setupDatePicker()
            loadRuangan()
            setupSpinners()
            setupSubmitButton()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Terjadi kesalahan saat menginisialisasi form")
        }
    }

    private fun setupSpinners() {
        try {
            activity?.let {
                it.window.statusBarColor = ContextCompat.getColor(it, R.color.ungu)
            }

            binding.dropdownKamarPasien.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    try {
                        val selectedRuanganName = parent?.getItemAtPosition(position)?.toString()
                        selectedRuanganName?.let {
                            loadPasienForRuangan(it)
                            updateJamKunjunganOptions(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Gagal memuat data ruangan")
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadRuangan() {
        if (!isViewActive) return

        ruanganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isViewActive || !isAdded) return

                try {
                    val ruanganList = mutableListOf<String>()
                    for (ruanganSnapshot in snapshot.children) {
                        val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                        ruangan?.nama_Ruangan?.let {
                            if (!ruanganList.contains(it)) {
                                ruanganList.add(it)
                            }
                        }
                    }

                    if (ruanganList.isEmpty()) {
                        ruanganList.add("No rooms available")
                    }

                    context?.let { ctx ->
                        val adapter = ArrayAdapter(
                            ctx,
                            android.R.layout.simple_spinner_item,
                            ruanganList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.dropdownKamarPasien.adapter = adapter

                        if (ruanganList.first() != "No rooms available") {
                            loadPasienForRuangan(ruanganList[0])
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Gagal memuat daftar ruangan")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isViewActive || !isAdded) return
                showError("Gagal memuat ruangan: ${error.message}")
            }
        })
    }

    private fun loadPasienForRuangan(ruanganName: String) {
        if (!isViewActive || !isAdded) return

        pasienRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isViewActive || !isAdded) return

                try {
                    val pasienList = mutableListOf<String>()
                    for (pasienSnapshot in snapshot.children) {
                        val pasien = pasienSnapshot.getValue(Pasien::class.java)
                        if (pasien?.nama_Ruangan == ruanganName && pasien.nama_Pasien != null) {
                            pasienList.add(pasien.nama_Pasien)
                        }
                    }

                    context?.let { ctx ->
                        val adapter = ArrayAdapter(
                            ctx,
                            android.R.layout.simple_spinner_item,
                            if (pasienList.isEmpty()) listOf("No patients available") else pasienList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.dropdownNamaPasien.adapter = adapter
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Gagal memuat daftar pasien")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isViewActive || !isAdded) return
                showError("Gagal memuat pasien: ${error.message}")
            }
        })
    }

    private fun updateJamKunjunganOptions(ruanganName: String) {
        if (!isViewActive || !isAdded) return

        ruanganRef.orderByChild("nama_Ruangan").equalTo(ruanganName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isViewActive || !isAdded) return

                    try {
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

                        context?.let { ctx ->
                            val adapter = ArrayAdapter(
                                ctx,
                                android.R.layout.simple_spinner_item,
                                jamKunjunganList
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.dropdownJamKunjungan.adapter = adapter
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError("Gagal memuat jam kunjungan")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isViewActive || !isAdded) return
                    showError("Gagal memperbarui jam kunjungan: ${error.message}")
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
        if (!isViewActive || !isAdded) return

        try {
            context?.let { ctx ->
                val datePickerDialog = DatePickerDialog(
                    ctx,
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
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Gagal menampilkan kalender")
        }
    }

    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            if (!isViewActive || isSubmitting) return@setOnClickListener

            try {
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
                        tanggal_kunjungan = selectedDate,
                        jam_kunjungan = jamKunjungan ?: "",
                        hubungan = hubungan
                    )

                    submitKunjungan(kunjungan)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Gagal memproses data kunjungan")
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
        if (!isViewActive) return false

        when {
            nama.isEmpty() -> {
                showError("Nama tidak boleh kosong")
                return false
            }
            kamarPasien == null || kamarPasien == "No rooms available" -> {
                showError("Pilih kamar pasien")
                return false
            }
            namaPasien == null || namaPasien == "No patients available" -> {
                showError("Pilih nama pasien")
                return false
            }
            tanggalKunjungan == 0L -> {
                showError("Pilih tanggal kunjungan")
                return false
            }
            jamKunjungan == null -> {
                showError("Pilih jam kunjungan")
                return false
            }
            hubungan.isEmpty() -> {
                showError("Hubungan tidak boleh kosong")
                return false
            }
        }
        return true
    }

    private fun submitKunjungan(kunjungan: Kunjungan) {
        if (!isViewActive || isSubmitting) return
        isSubmitting = true

        try {
            binding.buttonSubmit.isEnabled = false

            val newKunjunganRef = kunjunganRef.push()
            newKunjunganRef.setValue(kunjungan)
                .addOnSuccessListener {
                    if (!isViewActive || !isAdded) return@addOnSuccessListener

                    showSuccess("Kunjungan berhasil disubmit")

                    view?.post {
                        try {
                            if (isViewActive && isAdded) {
                                navigateToHome()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showError("Gagal berpindah halaman")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    if (!isViewActive || !isAdded) return@addOnFailureListener

                    e.printStackTrace()
                    showError("Gagal submit kunjungan: ${e.message}")
                    binding.buttonSubmit.isEnabled = true
                    isSubmitting = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
            if (isViewActive) {
                showError("Gagal mengirim data kunjungan")
                binding.buttonSubmit.isEnabled = true
                isSubmitting = false
            }
        }
    }

    private fun navigateToHome() {
        try {
            if (!isViewActive || !isAdded) return

            val homeFragment = user_home()
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, homeFragment)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Gagal berpindah ke halaman utama")
        }
    }

    private fun showError(message: String) {
        try {
            if (isViewActive && isAdded) {
                context?.let {
                    Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSuccess(message: String) {
        try {
            if (isViewActive && isAdded) {
                context?.let {
                    Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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