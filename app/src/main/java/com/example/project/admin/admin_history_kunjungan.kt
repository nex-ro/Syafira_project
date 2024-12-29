package com.example.project.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.databinding.FragmentAdminHistoryKunjunganBinding
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Dashboard
import com.example.project.Data.Kunjungan
import com.example.project.R
import com.example.project.admin.adapter.history_kj_Adapter
import com.google.firebase.database.*
import java.util.*

class admin_history_kunjungan : Fragment() {

    private lateinit var binding: FragmentAdminHistoryKunjunganBinding
    private lateinit var historyAdapter: history_kj_Adapter
    private lateinit var database: DatabaseReference
    private var KunjunganList: MutableList<Kunjungan> = mutableListOf()
    private var fullKunjunganList: MutableList<Kunjungan> = mutableListOf()
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHistoryKunjunganBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupDatabase()
        setupSearchView()
        setupSpinner()
        binding.backButton.setOnClickListener(){
            setCurrentFragment(Dashboard())
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPasien.apply {
            layoutManager = LinearLayoutManager(context)
            historyAdapter = history_kj_Adapter(KunjunganList)
            adapter = historyAdapter
        }
    }

    private fun setupDatabase() {
        database = FirebaseDatabase.getInstance().getReference("Kunjungan")
        fetchKunjunganData()
    }

    private fun setupSearchView() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                isSearching = searchText.isNotEmpty()

                if (isSearching) {
                    binding.spinnerBulan.setSelection(0)
                } else if (binding.spinnerBulan.selectedItemPosition == 0) {
                    binding.spinnerBulan.setSelection(1)
                }

                filterData(searchText)
                binding.clearIcon.visibility = if (isSearching) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearIcon.setOnClickListener {
            binding.searchInput.text.clear()
            isSearching = false
            binding.spinnerBulan.setSelection(1)
            filterData("")
        }
    }

    private fun setupSpinner() {
        val bulanList = arrayOf(
            "All time", "Today", "January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"
        )

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bulanList
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerBulan.adapter = adapter
        }

        binding.spinnerBulan.setSelection(1)

        binding.spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isSearching && position != 0) {
                    binding.spinnerBulan.setSelection(0)
                    return
                }
                filterData(binding.searchInput.text.toString().trim())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
    private fun fetchKunjunganData() {
        historyAdapter.setLoading(true)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullKunjunganList.clear()
                snapshot.children.forEach { data ->
                    data.getValue(Kunjungan::class.java)?.let { kunjungan ->
                        fullKunjunganList.add(kunjungan)
                    }
                }
                historyAdapter.setLoading(false)
                filterData(binding.searchInput.text.toString().trim())
            }

            override fun onCancelled(error: DatabaseError) {
                historyAdapter.setLoading(false)
                showNoDataMessage(true)
            }
        })
    }

    private fun filterData(query: String) {
        val selectedPosition = binding.spinnerBulan.selectedItemPosition

        val filteredList = fullKunjunganList.filter { kunjungan ->
            val matchesSearch = kunjungan.nama?.contains(query, ignoreCase = true) == true
            val matchesDate = when {
                selectedPosition == 0 -> true
                selectedPosition == 1 -> isToday(kunjungan.tanggal_kunjungan ?: 0L)
                else -> isInMonth(kunjungan.tanggal_kunjungan ?: 0L, selectedPosition - 1)
            }
            matchesSearch && matchesDate
        }

        KunjunganList.clear()
        KunjunganList.addAll(filteredList)
        historyAdapter.notifyDataSetChanged()

        showNoDataMessage(KunjunganList.isEmpty())
    }

    private fun showNoDataMessage(show: Boolean) {
        binding.noDataText.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewPasien.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun isInMonth(timestamp: Long, month: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) == month
    }
}
