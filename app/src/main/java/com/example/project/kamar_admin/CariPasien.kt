package com.example.project

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.History
import com.example.project.databinding.FragmentCariPasienBinding
import com.example.project.kamar_admin.HistoryAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class CariPasien : Fragment() {
    private lateinit var binding: FragmentCariPasienBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var spinnerBulan: Spinner
    private lateinit var database: DatabaseReference
    private var historyList: MutableList<History> = mutableListOf()
    private var fullHistoryList: MutableList<History> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCariPasienBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewPasien
        searchBar = binding.searchBar
        spinnerBulan = binding.spinnerBulan

        recyclerView.layoutManager = LinearLayoutManager(context)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        database = FirebaseDatabase.getInstance().getReference("history")

        // Set default bulan saat ini
        setupSpinnerBulan()
        fetchHistoryData(getCurrentMonth())

        // Tambahkan TextWatcher untuk Search Bar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString().trim(), getSelectedMonth())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun setupSpinnerBulan() {
        val bulanList = resources.getStringArray(R.array.bulan_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bulanList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = adapter

        spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // "Today" filter
                    filterData(searchBar.text.toString().trim(), -1) // Use -1 to denote Today filter
                } else {
                    val selectedMonth = position // Filter by month
                    filterData(searchBar.text.toString().trim(), selectedMonth)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set default to "Today" filter
        spinnerBulan.setSelection(0)
    }

    private fun fetchHistoryData(filterMonth: Int) {
        // Set loading state to true to show shimmer
        historyAdapter.setLoading(true)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullHistoryList.clear()

                for (data in snapshot.children) {
                    val history = data.getValue(History::class.java)
                    if (history != null && history.tanggal_Masuk != 0L && isInMonth(history, filterMonth)) {
                        fullHistoryList.add(history)
                    }
                }

                // Once the data is loaded, set loading to false
                historyAdapter.setLoading(false)

                filterData(searchBar.text.toString().trim(), filterMonth)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterData(query: String, filterMonth: Int) {
        historyList.clear()

        // Check if the filter is for "Today"
        val filteredHistory = if (filterMonth == -1) {
            // Filter by "Today"
            fullHistoryList.filter { history -> isToday(history) && history.nama?.contains(query, ignoreCase = true) == true }
        } else {
            // Filter by selected month
            fullHistoryList.filter { history -> history.nama?.contains(query, ignoreCase = true) == true && isInMonth(history, filterMonth) }
        }

        historyList.addAll(filteredHistory)
        historyAdapter.notifyDataSetChanged()
    }
    private fun isToday(history: History): Boolean {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        calendar.timeInMillis = history.tanggal_Masuk
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }


    private fun isInMonth(history: History, filterMonth: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = history.tanggal_Masuk  // Gunakan tanggal_Masuk dalam format Long

        val month = calendar.get(Calendar.MONTH) + 1  // Mengambil bulan dari calendar (1-12)
        return month == filterMonth
    }

    private fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    private fun getSelectedMonth(): Int {
        return spinnerBulan.selectedItemPosition + 1
    }
}
