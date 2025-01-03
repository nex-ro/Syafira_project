package com.example.project.statistikPage

import LaporanMedisAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.Laporan_Penanganan
import com.example.project.R
import com.example.project.databinding.FragmentLaporanMedisBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class laporan_medis : Fragment() {
    private var _binding: FragmentLaporanMedisBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private val laporanList = mutableListOf<Laporan_Penanganan>()
    private val filteredList = mutableListOf<Laporan_Penanganan>()
    private lateinit var adapter: LaporanMedisAdapter
    private val jenisList = listOf("Semua Jenis", "VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium", "ICU", "HCU")
    private var selectedJenis: String = "Semua Jenis"
    private var selectedPenyakit: String = "Semua Penyakit"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanMedisBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference.child("pasien")

        // Panggil setupRecyclerView agar adapter terinisialisasi
        setupRecyclerView()
        setupViews()
        fetchLaporanData()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TextWatcher pada searchBar
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLaporan()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Menutup fragment saat ini dan kembali ke fragment sebelumnya
        }
    }
    private fun setupViews() {
        setupRecyclerView()
        setupSearch()
        setupBackButton()
        setupTabs()
        setupFilterPenyakit()
    }

    private fun setupRecyclerView() {
        adapter = LaporanMedisAdapter(filteredList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLaporan()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun setupTabs() {
        val dynamicJenisList = mutableListOf("Semua Jenis")

        FirebaseDatabase.getInstance().reference.child("Ruangan")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { data ->
                        val jenis = data.child("jenis").value?.toString()
                        if (jenis != null && !dynamicJenisList.contains(jenis)) {
                            dynamicJenisList.add(jenis)
                        }
                    }

                    dynamicJenisList.forEach { jenis ->
                        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(jenis))
                    }

                    binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            filterLaporanByJenis(tab?.text.toString())
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {}
                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memuat jenis ruangan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupFilterPenyakit() {
        // Daftar penyakit umum yang ingin difilter
        val penyakitUmum = listOf("demam", "flu", "batuk", "radang tenggorokan", "asma")
        val penyakitList = mutableListOf("Semua Penyakit") // Awal dengan pilihan default

        // Tambahkan penyakit umum ke dalam daftar filter
        penyakitList.addAll(penyakitUmum.map { it.capitalize(Locale.getDefault()) })

        // Atur adapter untuk spinner
        binding.filterSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            penyakitList
        )

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val penyakit = it.child("penyakit").value?.toString()?.lowercase(Locale.getDefault())?.trim()
                    if (penyakit != null && !penyakitList.contains(penyakit.capitalize(Locale.getDefault())) && penyakitUmum.contains(penyakit)) {
                        penyakitList.add(penyakit.capitalize(Locale.getDefault()))
                    }
                }
                (binding.filterSpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Set listener untuk spinner
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterLaporanByPenyakit(penyakitList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchLaporanData() {
        val ruanganMap = mutableMapOf<String, String>()

        FirebaseDatabase.getInstance().reference.child("Ruangan")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { data ->
                        val namaRuangan = data.child("nama_Ruangan").value?.toString()?.lowercase(Locale.getDefault()) ?: ""
                        val jenis = data.child("jenis").value?.toString() ?: "Tidak diketahui"
                        ruanganMap[namaRuangan] = jenis
                    }

                    // Panggil fetchPasienData setelah mapping selesai
                    fetchPasienData(ruanganMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Gagal memuat data ruangan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun fetchPasienData(ruanganMap: Map<String, String>) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                laporanList.clear()
                snapshot.children.forEach { data ->
                    val laporan = data.getValue(Laporan_Penanganan::class.java)
                    Log.d("FetchData", "Laporan: $laporan") // Log data laporan
                    laporan?.let {
                        val namaRuangan = it.nama_Ruangan?.lowercase(Locale.getDefault()) ?: ""
                        Log.d("MappingRuangan", "Nama Ruangan: $namaRuangan -> Jenis: ${ruanganMap[namaRuangan]}")
                        it.jenis = ruanganMap[namaRuangan] ?: "Tidak diketahui"
                        laporanList.add(it)
                    }
                }
                filterLaporan()
            }



            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memuat data pasien: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    // Modifikasi fungsi filter utama
    private fun filterLaporan() {
        val query = binding.searchBar.text.toString().trim().lowercase(Locale.getDefault())
        filteredList.clear()
        filteredList.addAll(laporanList.filter { laporan ->
            val matchJenis = selectedJenis == "Semua Jenis" || laporan.jenis?.lowercase(Locale.getDefault()) == selectedJenis.lowercase(Locale.getDefault())
            val matchPenyakit = selectedPenyakit == "semua penyakit" || laporan.penyakit?.lowercase(Locale.getDefault()) == selectedPenyakit
            val matchQuery = laporan.nama_Pasien?.lowercase(Locale.getDefault())?.contains(query) == true
            matchJenis && matchPenyakit && matchQuery
        })
        adapter.notifyDataSetChanged()
    }







    private fun filterLaporanByJenis(jenis: String) {
        selectedJenis = jenis
        filterLaporan()
    }

    private fun filterLaporanByPenyakit(penyakit: String) {
        selectedPenyakit = penyakit.lowercase(Locale.getDefault()).trim()
        filterLaporan()
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
