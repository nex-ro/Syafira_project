package com.example.project

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.History
import com.example.project.databinding.FragmentAturKamarBinding
import com.example.project.databinding.FragmentCariPasienBinding
import com.example.project.kamar_admin.HistoryAdapter
import com.google.firebase.database.*

class CariPasien : Fragment() {
    private lateinit var binding: FragmentCariPasienBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var database: DatabaseReference
    private var historyList: MutableList<History> = mutableListOf()
    private var fullHistoryList: MutableList<History> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cari_pasien, container, false)
        binding = FragmentCariPasienBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewPasien
        searchBar = binding.searchBar
        recyclerView.layoutManager = LinearLayoutManager(context)

        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        database = FirebaseDatabase.getInstance().getReference("history")

        // Ambil data dari Firebase
        fetchHistoryData()

        // Tambahkan TextWatcher untuk Search Bar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    // Jika search bar kosong, tampilkan semua data
                    historyList.clear()
                    historyList.addAll(fullHistoryList)
                } else {
                    // Filter berdasarkan nama
                    historyList.clear()
                    historyList.addAll(
                        fullHistoryList.filter {
                            it.nama?.contains(query, ignoreCase = true) == true
                        }
                    )
                }
                historyAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun fetchHistoryData() {
        // Aktifkan mode loading
        historyAdapter.setLoading(true)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullHistoryList.clear()
                historyList.clear()

                for (data in snapshot.children) {
                    val history = data.getValue(History::class.java)
                    if (history != null) {
                        fullHistoryList.add(history)
                    }
                }

                // Setelah data diterima, matikan shimmer
                historyList.addAll(fullHistoryList)
                historyAdapter.setLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
