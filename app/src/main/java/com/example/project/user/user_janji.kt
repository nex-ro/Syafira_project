package com.example.project.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.R
import com.example.project.databinding.FragmentUserJanjiBinding
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.Kunjungan
import com.example.project.Login
import com.example.project.user.adapter.KunjunganAdapter
import com.google.firebase.database.*

class user_janji : Fragment() {
    private lateinit var binding: FragmentUserJanjiBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var kunjunganAdapter: KunjunganAdapter
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.bg)

        binding = FragmentUserJanjiBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        databaseRef = FirebaseDatabase.getInstance().getReference("Kunjungan")

        setupViews()
        return binding.root
    }

    private fun setupViews() {
        try {
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            val username = sharedPreferences.getString("username", "") ?: "Guest"
            val id = sharedPreferences.getString("id", "")

            binding.toolbarTitle.text = "Hi, $username"
            binding.toolbar.menu.findItem(R.id.quit)?.isVisible = isLoggedIn

            if (isLoggedIn && !id.isNullOrEmpty()) {
                setupRecyclerView()
                loadKunjunganData(id)
            } else {
                binding.empty.isVisible = true
                binding.recyclerView.isVisible = false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up views: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            kunjunganAdapter = KunjunganAdapter()
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = kunjunganAdapter
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up list: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadKunjunganData(userId: String) {
        try {
            databaseRef.orderByChild("id_pengunjung").equalTo(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val kunjunganList = mutableListOf<Kunjungan>()

                            for (kunjunganSnapshot in snapshot.children) {
                                val kunjungan = kunjunganSnapshot.getValue(Kunjungan::class.java)
                                kunjungan?.let { kunjunganList.add(it) }
                            }

                            binding.apply {
                                empty.isVisible = kunjunganList.isEmpty()
                                recyclerView.isVisible = kunjunganList.isNotEmpty()
                            }

                            if (kunjunganList.isNotEmpty()) {
                                kunjunganAdapter.updateData(kunjunganList.sortedByDescending { it.tanggal_kunjungan })
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error processing data: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.empty.isVisible = true
                        binding.recyclerView.isVisible = false
                        Toast.makeText(context, "Database error: ${error.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}