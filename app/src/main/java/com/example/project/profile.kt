package com.example.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class profile : Fragment() {
    private lateinit var ref: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        // Inflate the layout for this fragment
        ref = FirebaseDatabase.getInstance().reference

        // Tambahkan data ke Realtime Database
        addTestData()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    private fun addTestData() {
        val testData = "test"
        ref.child("messages").push().setValue(testData)
            .addOnSuccessListener {
                Log.d("Firebase", "Data berhasil ditambahkan!")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Gagal menambahkan data: ${exception.message}")
            }
    }
}