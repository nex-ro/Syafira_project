package com.example.project.Profil

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.project.Data.UserProfile
import com.example.project.R
import com.example.project.databinding.FragmentEditProfileBinding
import com.example.project.databinding.FragmentTambahProfileBinding
import com.example.project.profile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TambahProfile : Fragment() {

    private lateinit var ref: DatabaseReference
    private var _binding: FragmentTambahProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTambahProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase reference
        ref = FirebaseDatabase.getInstance().reference.child("user")

        // Binding views
        val editTextUsername = binding.editTextUsername
        val editTextName = binding.editTextName
        val editTextEmail = binding.editTextEmail
        val editTextPhone = binding.editTextPhone
        val editTextPassword = binding.editTextPassword
        val editTextConfirmPassword = binding.editTextConfirmPassword
        val radioGroupGender = binding.radioGroupGender
        val buttonCreateAccount = binding.buttonCreateAccount

        // Button click listener to create account
        buttonCreateAccount.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            // Check if all fields are filled
            if (username.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Check if email is valid
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.error = "Email tidak valid"
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(
                    requireContext(),
                    "Password dan Konfirmasi tidak cocok!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Check if the password meets the criteria: minimum 8 characters, contains at least one uppercase letter, and contains at least one number
            val passwordPattern = "^(?=.*[A-Z])(?=.*\\d).{8,}$"
            if (!password.matches(passwordPattern.toRegex())) {
                Toast.makeText(
                    requireContext(),
                    "Password harus memiliki minimal 8 karakter, satu huruf besar, dan satu angka",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Check if the username or email is already used in Firebase
            ref.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // If username already exists, show a message
                            Toast.makeText(
                                requireContext(),
                                "Username sudah terdaftar!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Check if the email already exists in Firebase
                            ref.orderByChild("email").equalTo(email)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            // If email already exists, show a message
                                            Toast.makeText(
                                                requireContext(),
                                                "Email sudah terdaftar!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            // If both username and email are unique, create the new user
                                            val gender =
                                                when (radioGroupGender.checkedRadioButtonId) {
                                                    R.id.rbMale -> "Laki-laki"
                                                    R.id.rbFemale -> "Perempuan"
                                                    else -> ""
                                                }

                                            // Create a new user object
                                            val newUser = UserProfile(
                                                username,
                                                name,
                                                email,
                                                phone,
                                                gender,
                                                password
                                            )

                                            // Save the user to Firebase Realtime Database
                                            ref.child(username).setValue(newUser)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        // Success: Account created
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Akun berhasil dibuat!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        requireActivity().supportFragmentManager.popBackStack() // Close the fragment after successful creation
                                                    } else {
                                                        // Error: Account creation failed
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Gagal membuat akun: ${task.exception?.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Error while checking the email
                                        Toast.makeText(
                                            requireContext(),
                                            "Gagal memeriksa email: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e("EmailCheck", "Error: ${error.message}")
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Error while checking the username
                        Toast.makeText(
                            requireContext(),
                            "Gagal memeriksa username: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("UsernameCheck", "Error: ${error.message}")
                    }
                })
        }
    }
}
    
    
