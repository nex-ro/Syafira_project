package com.example.project.statistikPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.databinding.FragmentStatistikMedisBinding
import com.google.firebase.database.*

class Statistik_Medis : Fragment() {
    private var _binding: FragmentStatistikMedisBinding? = null
    private val binding get() = _binding!! // Ensure non-nullable usage
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatistikMedisBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        fetchRoomStatus()
        binding.cardStatusRuangan.setOnClickListener {
            setCurrentFragment(laporan_medis())
        }
        binding.cardWaktuTunggu.setOnClickListener {
            setCurrentFragment(waktu_tunggu())
        }

        // Fetch waktu tunggu
        fetchWaktuTunggu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding to avoid memory leaks
    }

    private fun fetchRoomStatus() {
        val statusList = listOf("VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium", "ICU", "HCU")

        database.child("Ruangan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (status in statusList) {
                        val matchingRooms = snapshot.children.filter {
                            it.child("jenis").value == status
                        }
                        matchingRooms.forEach { roomSnapshot ->
                            val roomStatus = roomSnapshot.child("status").value.toString()
                            val roomjenis = roomSnapshot.child("jenis").value.toString()

                            if (isAdded && _binding != null) { // Ensure fragment is still active
                                updateUI(roomjenis, roomStatus)
                            }
                        }
                    }
                } else {
                    Log.e("FirebaseDebug", "Data snapshot tidak ditemukan.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchWaktuTunggu() {
        database.child("history_antrian").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Fetching data from history_antrian")

                if (snapshot.exists()) {
                    val waktuTungguMap = mutableMapOf<String, MutableList<Int>>()
                    snapshot.children.forEach { data ->
                        val kategori = data.child("jenis_ruangan").value.toString()
                        val waktuString = data.child("waktu_tunggu").value.toString().replace(" Menit", "")
                        val waktu = waktuString.toIntOrNull()

                        Log.d("FirebaseDebug", "jenis_ruangan: $kategori, Waktu String: $waktuString, Waktu: $waktu")

                        if (kategori.isNotBlank() && waktu != null) {
                            waktuTungguMap.putIfAbsent(kategori, mutableListOf())
                            waktuTungguMap[kategori]?.add(waktu)
                        } else {
                            Log.w("FirebaseDebug", "Invalid data found: Kategori=$kategori, Waktu=$waktu")
                        }
                    }

                    // Hitung rata-rata dan update UI
                    waktuTungguMap.forEach { (kategori, waktuList) ->
                        val rataRata = if (waktuList.isNotEmpty()) waktuList.average().toInt() else 0
                        Log.d("FirebaseDebug", "Kategori: $kategori, Rata-rata Waktu Tunggu: $rataRata")
                        updateWaktuTungguUI(kategori, rataRata)
                    }
                } else {
                    Log.e("FirebaseDebug", "Data history_antrian tidak ditemukan.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateWaktuTungguUI(kategori: String, rataRata: Int) {
        Log.d("FirebaseDebug", "Updating UI for kategori: $kategori with rata-rata: $rataRata Menit")

        val waktuTextView: TextView? = when (kategori) {
            "Radiologi" -> binding.root.findViewById(R.id.textRadiologi)
            "Laboratorium" -> binding.root.findViewById(R.id.textLaboratorium)
            "Farmasi" -> binding.root.findViewById(R.id.textFarmasi)
            else -> null
        }

        if (waktuTextView != null) {
            waktuTextView.text = "$rataRata Menit"
            Log.d("FirebaseDebug", "UI updated successfully for kategori: $kategori")
        } else {
            Log.w("FirebaseDebug", "TextView not found for kategori: $kategori")
        }
    }

    private fun updateUI(roomjenis: String, roomStatus: String) {
        // Ensure _binding is not null to prevent crashes
        if (_binding == null) return

        val statusTextView: TextView? = when (roomjenis) {
            "ICU" -> binding.root.findViewById(R.id.texticustatus)
            "Laboratorium" -> binding.root.findViewById(R.id.textlaborstatus)
            "HCU" -> binding.root.findViewById(R.id.texthcustatus)
            else -> null
        }

        statusTextView?.let {
            when (roomStatus) {
                "terisi" -> {
                    it.text = "Terisi"
                    it.setTextColor(resources.getColor(R.color.terisi, null))
                }
                "penuh" -> {
                    it.text = "Penuh"
                    it.setTextColor(resources.getColor(R.color.penuh, null))
                }
                "kosong" -> {
                    it.text = "Kosong"
                    it.setTextColor(resources.getColor(R.color.kosong, null))
                }
                else -> {
                    it.text = "Unknown"
                    it.setTextColor(resources.getColor(R.color.unknown, null))
                }
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
