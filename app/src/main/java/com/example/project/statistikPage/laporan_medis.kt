package com.example.project.statistikPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.Dashboard
import com.example.project.Data.Laporan_Penanganan
import com.example.project.R
import com.example.project.databinding.FragmentAdminKelolahKunjunganBinding
import com.example.project.databinding.FragmentLaporanMedisBinding
import com.example.project.databinding.FragmentStatistikMedisBinding
import com.google.firebase.database.*

class laporan_medis : Fragment() {
    private lateinit var binding: FragmentLaporanMedisBinding
    private lateinit var database: DatabaseReference
    private lateinit var laporanList: ArrayList<Laporan_Penanganan>
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dataList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_statistik__medis, container, false)
        binding = FragmentLaporanMedisBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        dataList = ArrayList()

        // Initialize adapter
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dataList)
        setupViews()
        return binding.root
    }
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            setCurrentFragment(Statistik_Medis())
        }
    }
    private fun setupViews() {
        setupBackButton()

    }

    private fun fetchLaporanData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                laporanList.clear()
                adapter.clear()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val laporan = data.getValue(Laporan_Penanganan::class.java)
                        if (laporan != null) {
                            laporanList.add(laporan)
                            // Format data to display in the ListView
                            val displayText =
                                "Penanganan: ${laporan.nama_Penanganan}\n" +
                                        "Created At: ${laporan.createdAt}\n" +
                                        "Lama Pelayanan: ${laporan.lama_Pelayanan} minutes"
                            adapter.add(displayText)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
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
