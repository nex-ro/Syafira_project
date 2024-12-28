package com.example.project

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Intent
import android.content.SharedPreferences
import com.example.project.login.Register
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import androidx.core.content.ContextCompat

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var ref: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ref = FirebaseDatabase.getInstance().reference.child("user")
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }
        binding.buttonExit.setOnClickListener{
            startActivity(Intent(this@Login, MainActivity::class.java))
            finish()
        }
        binding.buttonRegister.setOnClickListener(){
            startActivity(Intent(this@Login, Register::class.java))
            finish()
        }
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
                            null, null, ContextCompat.getDrawable(this, R.drawable.ic_visibility_off), null
                        )
                    } else {
                        binding.editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, ContextCompat.getDrawable(this, R.drawable.ic_visibility), null
                        )
                    }
                    binding.editTextPassword.setSelection(binding.editTextPassword.text?.length ?: 0)
                    return@setOnTouchListener true
                }
            }
            false
        }

    }

    private fun loginUser(username: String, password: String) {
        ref.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val storedPassword = snapshot.child("password").value.toString()

                    if (storedPassword == password) {
                        val fullName = snapshot.child("fullName").value?.toString() ?: ""
                        val email = snapshot.child("email").value?.toString() ?: ""
                        val phone = snapshot.child("phone").value?.toString() ?: ""
                        val gender = snapshot.child("gender").value?.toString() ?: ""

                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("username", username)
                        editor.putString("nama", fullName)
                        editor.putString("email", email)
                        editor.putString("phone", phone)
                        editor.putString("gender", gender)
                        editor.apply()

                        Toast.makeText(
<<<<<<< HEAD:app/src/main/java/com/example/project/login/Login.kt
                            this@Login,
                            "Login successful! Welcome, $nama",
=======
                            this@login,
                            "Login successful! Welcome, $fullName",
>>>>>>> f861ae1b275756983e336cde0a3cfb8822596817:app/src/main/java/com/example/project/login/login.kt
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Invalid password!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Login, "User not found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
                Toast.makeText(this@Login, "Login failed. Try again later.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
