package com.example.project

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class profile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var ref: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        databaseReference = FirebaseDatabase.getInstance().getReference("user")

        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Inisialisasi ActivityResultLauncher untuk memilih gambar
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                binding.imgProfile.setImageURI(imageUri)
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
        val fullName = sharedPreferences.getString("nama", null)
        val email = sharedPreferences.getString("email", null)
        val phone = sharedPreferences.getString("phone", null)
        val gender = sharedPreferences.getString("gender", null)

        // Set data ke views
        binding.txtGreeting.text = "Hi, $username!"  // Menampilkan username
        binding.editTextUsername.setText(username)
        binding.edtFullName.setText(fullName)
        binding.edtEmail.setText(email)
        binding.edtNoHP.setText(phone)
        binding.edtJenisKelamin.setText(gender)

    }

    private fun saveUserProfile() {
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            val updatedUsername = binding.editTextUsername.text.toString()
            val updatedFullName = binding.edtFullName.text.toString()
            val updatedEmail = binding.edtEmail.text.toString()
            val updatedPhone = binding.edtNoHP.text.toString()
            val updatedGender = binding.edtJenisKelamin.text.toString()

            val userProfile = mapOf(
                "username" to updatedUsername,
                "fullName" to updatedFullName,
                "email" to updatedEmail,
                "phone" to updatedPhone,
                "gender" to updatedGender
            )

            databaseReference.child(username).setValue(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Simpan data yang baru ke SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putString("username", updatedUsername)
                        editor.putString("nama", updatedFullName)
                        editor.putString("email", updatedEmail)
                        editor.putString("phone", updatedPhone)
                        editor.putString("gender", updatedGender)
                        editor.apply() // Simpan perubahan

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
        getContent.launch(intent)
    }

    private fun logoutUser() {
        // Clear SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}