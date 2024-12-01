package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Ruangan
import com.example.project.Data.cardData
import com.example.project.databinding.FragmentKamarBinding
import com.example.project.kamarPage.AturKamar
import com.example.project.kamarPage.TambahKamar
import com.example.project.kamarPage.kamarJenis
import com.google.firebase.database.*

class kamar : Fragment() {
    private lateinit var ref: DatabaseReference
    private lateinit var binding: FragmentKamarBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter

    private val items = mutableListOf<Ruangan>() // Mutable list untuk menyimpan data

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKamarBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = ItemAdapter(listOf()) { jenis ->
            val detailFragment = kamarJenis.newInstance(jenis)
            setCurrentFragment(detailFragment)
        }
        recyclerView.adapter = adapter

        ref = FirebaseDatabase.getInstance().reference.child("Ruangan")

        // Mendapatkan data dari Firebase
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val groupedData = mutableMapOf<String, MutableList<Ruangan>>()

                    for (dataSnapshot in snapshot.children) {
                        val ruangan = dataSnapshot.getValue(Ruangan::class.java)
                        if (ruangan != null && ruangan.jenis != null) {
                            groupedData.getOrPut(ruangan.jenis) { mutableListOf() }.add(ruangan)
                        }
                    }

                    val resultList = groupedData.map { entry ->
                        val jenis = entry.key
                        val list = entry.value
                        val kosongCount = list.count { it.status == "kosong" }
                        val terisiCount = list.count { it.status == "terisi" }

                        cardData(
                            jenis = jenis,
                            kosong = kosongCount,
                            terisi = terisiCount
                        )
                    }

                    adapter.updateData(resultList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })

        binding.buttonTambahPasien.setOnClickListener {
            val AturKamar = AturKamar()
            setCurrentFragment(AturKamar)
        }
        binding.buttonTambahRuangan.setOnClickListener {
            val tambahKamar = TambahKamar()
            setCurrentFragment(tambahKamar)
        }

        return binding.root
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}
