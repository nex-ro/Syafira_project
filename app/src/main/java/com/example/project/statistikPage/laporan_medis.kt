package com.example.project.statistikPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.Data.Laporan_Penanganan
import com.example.project.R
import com.google.firebase.database.*

class laporan_medis : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var laporanList: ArrayList<Laporan_Penanganan>
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_laporan_medis, container, false)

        // Initialize ListView and Firebase database reference

        database = FirebaseDatabase.getInstance().reference.child("Laporan_Penanganan")
        laporanList = ArrayList()

        // Initialize ArrayAdapter for ListView
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList())
        listView.adapter = adapter



        return view
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
}
