package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Firebase Realtime Database
        ref = FirebaseDatabase.getInstance().reference

        // Tambahkan data ke Realtime Database
//        addTestData()
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
