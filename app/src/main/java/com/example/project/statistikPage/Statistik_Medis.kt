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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardStatusRuangan.setOnClickListener {
            setCurrentFragment(laporan_medis())
        }
        binding.cardWaktuTunggu.setOnClickListener {
            setCurrentFragment(waktu_tunggu())
        }
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
