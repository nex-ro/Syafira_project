package com.example.project.statistikPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.Data.Laporan_Penanganan
import com.google.firebase.database.*

class Statistik_Medis : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dataList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_statistik__medis, container, false)


        database = FirebaseDatabase.getInstance().reference
        dataList = ArrayList()

        // Initialize adapter
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dataList)

        // Fetch data and calculate statistics
        fetchLaporanData()

        return view
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
}
