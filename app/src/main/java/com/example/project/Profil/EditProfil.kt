package com.example.project.Profil

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
import com.example.project.R
import com.example.project.databinding.FragmentEditProfileBinding
import com.example.project.databinding.FragmentKamarAdmBinding
import com.example.project.databinding.FragmentProfileBinding
import com.example.project.kamar_adm
import com.example.project.Login
import com.example.project.profile
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfil : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout dengan ViewBinding
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Inisialisasi Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("user")

        // Inisialisasi SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Inisialisasi ActivityResultLauncher untuk memilih gambar
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                binding.imgProfile.setImageURI(imageUri) // Set gambar yang dipilih ke ImageView
            }
        }

        // Listener untuk memilih gambar
        binding.imgProfile.setOnClickListener {
            openImagePicker()
        }

        // Listener untuk tombol logout

        // Listener untuk tombol simpan profil
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
        binding.buttonBack.setOnClickListener {
            // Gunakan FragmentManager untuk mengganti fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, profile()) // Pastikan ID ini sesuai dengan container di layout
                .addToBackStack(null) // Opsional, tambahkan ke backstack agar bisa kembali
                .commit()
        }

        // Load data user saat fragment dimuat
        loadUserData()

        return binding.root
    }

    private fun loadUserData() {
        // Ambil data dari SharedPreferences
        val username = sharedPreferences.getString("username", "")
        val fullName = sharedPreferences.getString("nama", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val gender = sharedPreferences.getString("gender", "")

        // Set data ke EditText dan TextView
        binding.txtGreeting.text = "Hi, $username!"
        binding.editTextUsername.setText(username)
        binding.edtFullName.setText(fullName)
        binding.edtEmail.setText(email)
        binding.edtNoHP.setText(phone)

        // Set data ke RadioButton
        if (gender == "Laki-laki") {
            binding.rbLakiLaki.isChecked = true
        } else if (gender == "Perempuan") {
            binding.rbPerempuan.isChecked = true
        }
    }

    private fun saveUserProfile() {
        val username = sharedPreferences.getString("username", "")

        if (!username.isNullOrEmpty()) {
            // Ambil data dari input user
            val updatedUsername = binding.editTextUsername.text.toString()
            val updatedFullName = binding.edtFullName.text.toString()
            val updatedEmail = binding.edtEmail.text.toString()
            val updatedPhone = binding.edtNoHP.text.toString()

            // Validasi Email
            if (!updatedEmail.contains("@")) {
                Toast.makeText(requireContext(), "Email tidak valid!", Toast.LENGTH_SHORT).show()
                return
            }

            // Validasi No HP
            if (updatedPhone.length > 12) {
                Toast.makeText(requireContext(), "Nomor HP tidak valid!", Toast.LENGTH_SHORT).show()
                return
            }


            // Ambil jenis kelamin yang dipilih
            val updatedGender = if (binding.rbLakiLaki.isChecked) {
                "Laki-laki"
            } else if (binding.rbPerempuan.isChecked) {
                "Perempuan"
            } else {
                ""
            }

            // Ambil data lama (sebelum update)
            val oldFullName = sharedPreferences.getString("nama", "")
            val oldEmail = sharedPreferences.getString("email", "")
            val oldPhone = sharedPreferences.getString("phone", "")
            val oldGender = sharedPreferences.getString("gender", "")

            // Prepare a map for the fields to update
            val userProfile = mutableMapOf<String, Any>()

            // Only update fields if they have changed
            if (updatedUsername != username) userProfile["username"] = updatedUsername
            if (updatedFullName != oldFullName) userProfile["fullName"] = updatedFullName
            if (updatedEmail != oldEmail) userProfile["email"] = updatedEmail
            if (updatedPhone != oldPhone) userProfile["phone"] = updatedPhone
            if (updatedGender != oldGender) userProfile["gender"] = updatedGender

            // Update profile ke Firebase jika ada perubahan
            if (userProfile.isNotEmpty()) {
                databaseReference.child(username).updateChildren(userProfile)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Simpan ke SharedPreferences jika ada perubahan
                            val editor = sharedPreferences.edit()

                            // Only update if field has changed
                            if (updatedUsername != username) editor.putString("username", updatedUsername)
                            if (updatedFullName != oldFullName) editor.putString("nama", updatedFullName)
                            if (updatedEmail != oldEmail) editor.putString("email", updatedEmail)
                            if (updatedPhone != oldPhone) editor.putString("phone", updatedPhone)
                            if (updatedGender != oldGender) editor.putString("gender", updatedGender)

                            editor.apply()

                            Toast.makeText(requireContext(), "Profile berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menyimpan profil: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Tidak ada perubahan data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(requireActivity(), Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
