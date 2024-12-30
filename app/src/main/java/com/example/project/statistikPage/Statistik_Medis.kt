package com.example.project.statistikPage

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.Data.Laporan_Penanganan
import com.example.project.admin.admin_history_kunjungan
import com.example.project.databinding.FragmentDashboardBinding
import com.example.project.databinding.FragmentStatistikMedisBinding

import com.google.firebase.database.*

class Statistik_Medis : Fragment() {
    // Deklarasikan binding
    private var _binding: FragmentStatistikMedisBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dataList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            val view = inflater.inflate(R.layout.fragment_statistik__medis, container, false)
            _binding = FragmentStatistikMedisBinding.inflate(inflater, container, false)
            database = FirebaseDatabase.getInstance().reference
            dataList = ArrayList()

            // Initialize adapter
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dataList)

            // Fetch data and calculate statistics
            fetchLaporanData()
            return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cardStatusLayanan.setOnClickListener(){
            setCurrentFragment(laporan_medis())
        }
    }
    private fun fetchLaporanData() {
        database.child("Laporan_Penanganan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalWaktu = 0
                var jumlahLayanan = 0

                for (data in snapshot.children) {
                    val laporan = data.getValue(Laporan_Penanganan::class.java)
                    if (laporan != null && laporan.lama_Pelayanan != null) {
                        totalWaktu += laporan.lama_Pelayanan
                        jumlahLayanan++
                    }
                }

                // Hitung rata-rata waktu tunggu
                val rataRataWaktu = if (jumlahLayanan > 0) totalWaktu / jumlahLayanan else 0

                // Tambahkan data ke dalam list
                dataList.clear()
                dataList.add("Status Layanan: Rata-rata waktu tunggu adalah $rataRataWaktu menit")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
