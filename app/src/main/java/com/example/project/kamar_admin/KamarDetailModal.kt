package com.example.project.kamar_admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.Ruangan
import com.example.project.Data.Pasien
import com.example.project.R
import com.example.project.databinding.FragmentKamarDetailModalBinding
import com.example.project.kamar_adm
import com.example.project.kamar_admin.detail.PasienAdapter
import com.google.firebase.database.*

class KamarDetailModal : Fragment() {
    private lateinit var binding: FragmentKamarDetailModalBinding
    private lateinit var idRuangan: String
    private var nomorRuangan: Int = 0
    private lateinit var jenis: String
    private lateinit var namaRuangan: String
    private lateinit var status: String
    private var lantai: Int = 0
    private lateinit var ref_pasien: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idRuangan = it.getString("id_Ruangan", "")
            nomorRuangan = it.getInt("nomor_Ruangan", 0)
            jenis = it.getString("jenis", "")
            namaRuangan = it.getString("nama_Ruangan", "")
            status = it.getString("status", "")
            lantai = it.getInt("lantai", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentKamarDetailModalBinding.inflate(inflater, container, false)
        binding.TextDetail.text = "Kamar $namaRuangan"
        binding.backButton.setOnClickListener {
            setCurrentFragment(kamar_adm())
        }

        ref_pasien = FirebaseDatabase.getInstance().reference.child("pasien")
        Log.d("KamarDetailModal", "Nama Ruangan: $namaRuangan")
        fetchDataPasien()
        return binding.root
    }

    private fun fetchDataPasien() {
        ref_pasien.orderByChild("nama_Ruangan").equalTo(namaRuangan)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val pasienList = mutableListOf<Pasien>()
                        for (data in snapshot.children) {
                            val pasien = data.getValue(Pasien::class.java)
                            if (pasien != null) {
                                pasienList.add(pasien)
                            }
                        }

                        if (pasienList.isEmpty()) {
                            showEmptyState()
                        } else {
                            showPasienList(pasienList)
                        }
                        Log.d("KamarDetailModal", "Data Pasien: $pasienList")
                    } else {
                        showEmptyState()
                        Log.d("KamarDetailModal", "No data found for ruangan: $namaRuangan")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("KamarDetailModal", "Firebase error: ${error.message}")
                    showEmptyState()
                }
            })
    }

    private fun showEmptyState() {
        binding.recyclerViewPasien.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showPasienList(pasienList: List<Pasien>) {
        binding.recyclerViewPasien.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        setupRecyclerView(pasienList)
    }

    private fun setupRecyclerView(pasienList: List<Pasien>) {
        binding.recyclerViewPasien.layoutManager = LinearLayoutManager(context)
        val adapter = PasienAdapter(
            pasienList,
            onPindahRuanganClick = { pasien ->
                val dialog = PindahKamar(pasien)
                dialog.show(parentFragmentManager, "PindahKamarDialog")            }
        )
        binding.recyclerViewPasien.adapter = adapter
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }

    companion object {
        fun newInstance(ruangan: Ruangan): KamarDetailModal {
            val fragment = KamarDetailModal()
            val args = Bundle().apply {
                putString("id_Ruangan", ruangan.id_Ruangan)
                putInt("nomor_Ruangan", ruangan.nomor_Ruangan ?: 0)
                putString("jenis", ruangan.jenis)
                putString("nama_Ruangan", ruangan.nama_Ruangan)
                putString("status", ruangan.status)
                putInt("lantai", ruangan.lantai ?: 0)
                putInt("kapasitas", ruangan.kapasitas ?: 1)
            }
            fragment.arguments = args
            return fragment
        }
    }
}