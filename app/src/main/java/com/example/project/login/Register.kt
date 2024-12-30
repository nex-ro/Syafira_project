package com.example.project.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.ActivityRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.example.project.Data.UserProfile
import com.example.project.Login

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.ungu2)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ref = FirebaseDatabase.getInstance().reference.child("user")

        setupPasswordVisibilityToggle()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            registerUser()
        }

        binding.buttonExit.setOnClickListener {
            startActivity(Intent(this@Register, MainActivity::class.java))
            finish()
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this@Register, Login::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val username = binding.editTxtUsername.text.toString().trim()
        val fullName = binding.editTxtFullname.text.toString().trim()
        val email = binding.editTxtEmail.text.toString().trim()
        val phone = binding.editTxtnoHP.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.radioMale -> "Laki-laki"
            R.id.radioFemale -> "Perempuan"
            else -> ""
        }

        // Validation
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() ||
            phone.isEmpty() || password.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if username already exists
        ref.child(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Username sudah digunakan!", Toast.LENGTH_SHORT).show()
            } else {
                // Create user profile
                val userProfile = UserProfile(
                    username = username,
                    fullName = fullName,
                    password = password,
                    email = email,
                    phone = phone,
                    gender = gender
                )

                // Save to Firebase
                ref.child(username).setValue(userProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Register, Login::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("RegisterError", "Error: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("RegisterError", "Error: ${e.message}")
        }
    }

    private fun setupPasswordVisibilityToggle() {
        binding.editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(
            null, null, ContextCompat.getDrawable(this, R.drawable.ic_visibility_off), null
        )

        binding.editTextPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.editTextPassword.compoundDrawables[2]
                if (event.rawX >= (binding.editTextPassword.right - drawableEnd.bounds.width())) {
                    val isVisible = binding.editTextPassword.transformationMethod is HideReturnsTransformationMethod
                    if (isVisible) {
                        binding.editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            ContextCompat.getDrawable(this, R.drawable.ic_visibility_off),
                            null
                        )
                    } else {
                        binding.editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null, null,
                            ContextCompat.getDrawable(this, R.drawable.ic_visibility),
                            null
                        )
                    }
                    binding.editTextPassword.setSelection(binding.editTextPassword.text?.length ?: 0)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}