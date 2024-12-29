package com.example.project

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.History
import com.example.project.databinding.FragmentCariPasienBinding
import com.example.project.kamar_admin.HistoryAdapter
import com.google.firebase.database.*
import java.util.*

class CariPasien : Fragment() {
    private lateinit var binding: FragmentCariPasienBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var database: DatabaseReference
    private var historyList: MutableList<History> = mutableListOf()
    private var fullHistoryList: MutableList<History> = mutableListOf()
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCariPasienBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupDatabase()
        setupSearchView()
        setupSpinner()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPasien.apply {
            layoutManager = LinearLayoutManager(context)
            historyAdapter = HistoryAdapter(historyList)
            adapter = historyAdapter
        }
    }

    private fun setupDatabase() {
        database = FirebaseDatabase.getInstance().getReference("history")
        fetchHistoryData()
    }

    private fun setupSearchView() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                isSearching = searchText.isNotEmpty()

                // Automatically set spinner to "All time" when searching
                if (isSearching) {
                    binding.spinnerBulan.setSelection(0)
                } else if (binding.spinnerBulan.selectedItemPosition == 0) {
                    // If search is empty and spinner is on "All time", set it back to "Today"
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
            // When clearing search, set back to "Today"
            binding.spinnerBulan.setSelection(1)
            filterData("")
        }
    }

    private fun setupSpinner() {
        val bulanList = arrayOf("All time", "Today", "January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December")

        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanList).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerBulan.adapter = adapter
        }

        // Set default selection to "Today"
        binding.spinnerBulan.setSelection(1)

        binding.spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // If user is searching, force "All time" selection
                if (isSearching && position != 0) {
                    binding.spinnerBulan.setSelection(0)
                    return
                }
                filterData(binding.searchInput.text.toString().trim())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchHistoryData() {
        historyAdapter.setLoading(true)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullHistoryList.clear()
                snapshot.children.forEach { data ->
                    data.getValue(History::class.java)?.let { history ->
                        if (history.tanggal_Masuk != 0L) {
                            fullHistoryList.add(history)
                        }
                    }
                }
                historyAdapter.setLoading(false)
                filterData(binding.searchInput.text.toString().trim())
            }

            override fun onCancelled(error: DatabaseError) {
                historyAdapter.setLoading(false)
            }
        })
    }

    private fun filterData(query: String) {
        val selectedPosition = binding.spinnerBulan.selectedItemPosition

        val filteredList = fullHistoryList.filter { history ->
            val matchesSearch = history.nama?.contains(query, ignoreCase = true) == true
            val matchesDate = when {
                selectedPosition == 0 -> true // All time
                selectedPosition == 1 -> isToday(history.tanggal_Masuk) // Today
                else -> isInMonth(history.tanggal_Masuk, selectedPosition - 1) // Months (adjusted for new spinner positions)
            }
            matchesSearch && matchesDate
        }

        historyList.clear()
        historyList.addAll(filteredList)
        historyAdapter.notifyDataSetChanged()
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
        return calendar.get(Calendar.MONTH) + 1 == month
    }
}