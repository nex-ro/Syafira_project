package com.example.project.statistikPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.R

import com.example.project.databinding.FragmentWaktuTungguBinding
import com.example.project.Data.AntrianHistory
import com.example.project.Data.WaktuTungguDisplay
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.tabs.TabLayout

class waktu_tunggu : Fragment() {
    private var _binding: FragmentWaktuTungguBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private val waktuTungguList = mutableListOf<WaktuTungguDisplay>()
    private val filteredList = mutableListOf<WaktuTungguDisplay>() // List untuk filter data
    private lateinit var tabLayout: TabLayout
    private var selectedJenisRuangan: String? = null
    private var selectedStatus: String? = null
    private lateinit var chartFragment: WaktuTungguChartFragment


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaktuTungguBinding.inflate(inflater, container, false)
        setupViews() // Menyiapkan tab dan tampilan
        setupFilters()
        fetchAntrianData()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupViews() {
        setupBackButton()
        binding.recyclerViewWaktuTunggu.layoutManager = LinearLayoutManager(requireContext())

        chartFragment = WaktuTungguChartFragment()
        childFragmentManager.beginTransaction()
            .add(R.id.chartContainer, chartFragment)
            .commit()

        val viewTypeTabs = binding.root.findViewById<TabLayout>(R.id.tabLayoutViewType)
        viewTypeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.text == "Grafik") {
                    binding.recyclerViewWaktuTunggu.visibility = View.GONE
                    binding.chartContainer.visibility = View.VISIBLE
                    chartFragment.updateChartData(filteredList)
                } else {
                    binding.recyclerViewWaktuTunggu.visibility = View.VISIBLE
                    binding.chartContainer.visibility = View.GONE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun setupFilters() {
        val jenisRuanganOptions = listOf("Semua", "Radiologi", "Laboratorium", "Farmasi")
        val jenisRuanganAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisRuanganOptions)
        jenisRuanganAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenisRuangan.adapter = jenisRuanganAdapter

        binding.spinnerJenisRuangan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position).toString()
                selectedJenisRuangan = if (item == "Semua") null else item
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup TabLayout
        tabLayout = binding.root.findViewById(R.id.tabLayoutStatus)
        val tabs = listOf("Semua", "Selesai", "Menunggu", "Dibatalkan")
        for (tab in tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedStatus = if (tab?.text == "Semua") null else tab?.text.toString()
                applyFilters()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchAntrianData() {
        database = FirebaseDatabase.getInstance().getReference("history_antrian")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                waktuTungguList.clear()
                for (data in snapshot.children) {
                    val antrian = data.getValue(AntrianHistory::class.java)
                    if (antrian != null) {
                        waktuTungguList.add(
                            WaktuTungguDisplay(
                                jenisRuangan = antrian.jenis_ruangan ?: "",
                                waktuTunggu = antrian.waktu_tunggu ?: "",
                                nomorAntrian = antrian.nomor_antrian ?: "",
                                status = antrian.status ?: "",
                                tanggalMasuk = antrian.tanggal_masuk ?: 0L,
                            )
                        )
                    }
                }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilters() {
        filteredList.clear()
        filteredList.addAll(
            waktuTungguList.filter { antrian ->
                (selectedJenisRuangan == null || antrian.jenisRuangan == selectedJenisRuangan) &&
                        (selectedStatus == null || antrian.status == selectedStatus)
            }
        )

        if (binding.chartContainer.visibility == View.VISIBLE) {
            chartFragment.updateChartData(filteredList)
        } else {
            binding.recyclerViewWaktuTunggu.adapter = WaktuTungguAdapter(filteredList)
        }
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

