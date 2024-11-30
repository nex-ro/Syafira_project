package com.example.project

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Inisialisasi Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // SharedPreferences untuk session
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Inisialisasi ActivityResultLauncher untuk memilih gambar
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                binding.imgProfile.setImageURI(imageUri)  // Set gambar ke ImageView
            }
        }

        // Memanggil Intent untuk memilih gambar
        binding.imgProfile.setOnClickListener {
            openImagePicker()
        }

        // Logout Action
        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }

        // Simpan profil
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }

        // Load profil pengguna
        loadUserData()

        return binding.root
    }

    private fun loadUserData() {
        // Ambil username dari SharedPreferences
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            // Ambil data dari Firebase berdasarkan username
            databaseReference.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Jennie Nurlela"
                        val email = snapshot.child("email").getValue(String::class.java) ?: "jennie@gmaul.com"
                        val phone = snapshot.child("phone").getValue(String::class.java) ?: "08888887"
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: "Perempuan"

                        // Set data ke views
                        binding.txtGreeting.text = "Hi, $username!"  // Menampilkan username
                        binding.editTextUsername.setText(username)
                        binding.edtFullName.setText(fullName)
                        binding.edtEmail.setText(email)
                        binding.edtNoHP.setText(phone)
                        binding.edtJenisKelamin.setText(gender)
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "No username in session!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfile() {
        // Ambil username dari SharedPreferences
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            // Data profil yang akan disimpan, termasuk data yang diubah oleh user
            val userProfile = mapOf(
                "username" to binding.editTextUsername.text.toString(),
                "fullName" to binding.edtFullName.text.toString(),
                "email" to binding.edtEmail.text.toString(),
                "phone" to binding.edtNoHP.text.toString(),
                "gender" to binding.edtJenisKelamin.text.toString()
            )

            // Simpan data ke Firebase Database
            databaseReference.child(username).setValue(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "No username in session!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        getContent.launch(intent)  // Gunakan launcher untuk memulai aktivitas
    }

    private fun logoutUser() {
        // Clear SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect ke MainActivity
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}