package com.example.project.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.Pasien
import com.example.project.R
import com.example.project.databinding.FragmentUserCariPasienBinding
import com.example.project.user.adapter.PatientAdapter
import com.google.firebase.database.*

class user_cari_Pasien : Fragment() {
    private var _binding: FragmentUserCariPasienBinding? = null
    private val binding get() = _binding!!
    private lateinit var patientAdapter: PatientAdapter
    private lateinit var database: DatabaseReference
    private var patientList: MutableList<Pasien> = mutableListOf()
    private var originalList: MutableList<Pasien> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserCariPasienBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupSearch()
        setupDatabase()
    }

    private fun setupViews() {
        // Initialize RecyclerView and Adapter
        patientAdapter = PatientAdapter(patientList) { patient ->
            navigateToDetail(patient)
        }

        binding.recyclerViewPasien.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = patientAdapter
            setHasFixedSize(true)
        }

        // Setup back navigation
        binding.logoImageView.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPatients(s.toString())
                updateClearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearIcon.setOnClickListener {
            clearSearch()
        }
    }

    private fun updateClearButtonVisibility(s: CharSequence?) {
        binding.clearIcon.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }

    private fun clearSearch() {
        binding.searchInput.text.clear()
        binding.clearIcon.visibility = View.GONE
        resetToOriginalList()
    }

    private fun resetToOriginalList() {
        patientList.clear()
        patientList.addAll(originalList)
        patientAdapter.notifyDataSetChanged()
    }

    private fun filterPatients(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { patient ->
                patient.nama_Pasien?.contains(query, ignoreCase = true) == true ||
                        patient.nama_Ruangan?.contains(query, ignoreCase = true) == true
            }
        }
        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(filteredList: List<Pasien>) {
        patientList.clear()
        patientList.addAll(filteredList)
        patientAdapter.notifyDataSetChanged()
    }

    private fun setupDatabase() {
        try {
            database = FirebaseDatabase.getInstance().getReference("pasien")
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleDatabaseUpdate(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    handleDatabaseError(error)
                }
            }
            database.addValueEventListener(valueEventListener)
        } catch (e: Exception) {
            Log.e("DatabaseSetup", "Error setting up database", e)
        }
    }

    private fun handleDatabaseUpdate(snapshot: DataSnapshot) {
        try {
            patientList.clear()
            originalList.clear()

            for (dataSnapshot in snapshot.children) {
                val patient = dataSnapshot.getValue(Pasien::class.java)
                patient?.let {
                    patientList.add(it)
                    originalList.add(it)
                }
            }

            patientAdapter.notifyDataSetChanged()
            Log.d("DatabaseUpdate", "Loaded ${patientList.size} patients")

            // Update UI based on data presence
            updateEmptyState(patientList.isEmpty())

        } catch (e: Exception) {
            Log.e("DatabaseUpdate", "Error processing data", e)
        }
    }

    private fun handleDatabaseError(error: DatabaseError) {
        Log.e("DatabaseError", "Error: ${error.message}")
        // Here you could show an error message to the user
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        // Implement empty state handling if needed
    }

    private fun navigateToDetail(patient: Pasien) {
        try {
            Log.d("Navigation", "Navigating to detail for patient: ${patient.nama_Pasien}")

            val formFragment = user_form_Kunjungan().apply {
                arguments = Bundle().apply {
                    putString("nama_pasien", patient.nama_Pasien)
                    putString("nama_ruangan", patient.nama_Ruangan)
                }
            }

            parentFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, formFragment)
                    addToBackStack(null)
                    commit()
                }

        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to detail", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }

    companion object {
        private const val TAG = "user_cari_Pasien"
    }
}