package com.example.project.Profil

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.Data.UserProfile
import com.example.project.R
import com.example.project.databinding.FragmentTambahProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UbahPassword : Fragment() {

    private lateinit var ref: DatabaseReference
    private var _binding: UbahPassword? = null
    private val binding get() = _binding!!


    private lateinit var passwordLamaEditText: EditText
    private lateinit var passwordBaruEditText: EditText
    private lateinit var passwordKonfirmasiEditText: EditText
    private lateinit var buttonUbahPassword: Button
    private lateinit var passwordRulesTextView: TextView

    private lateinit var currentUserUsername: String // Username pengguna yang sedang login

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ubah_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase reference
        ref = FirebaseDatabase.getInstance().reference.child("user")

        // Inisialisasi views
        passwordLamaEditText = view.findViewById(R.id.passwordLama)
        passwordBaruEditText = view.findViewById(R.id.passwordBaru)
        passwordKonfirmasiEditText = view.findViewById(R.id.passwordKonfirmasi)
        buttonUbahPassword = view.findViewById(R.id.buttonUbahPassword)
        passwordRulesTextView = view.findViewById(R.id.password_rules)

        // Ambil username yang sedang login dari SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        currentUserUsername = sharedPref.getString("username", "") ?: ""

        if (currentUserUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        // Set klik tombol untuk ubah password
        buttonUbahPassword.setOnClickListener {
            val passwordLama = passwordLamaEditText.text.toString().trim()
            val passwordBaru = passwordBaruEditText.text.toString().trim()
            val passwordKonfirmasi = passwordKonfirmasiEditText.text.toString().trim()

            // Validasi inputan
            if (TextUtils.isEmpty(passwordLama) || TextUtils.isEmpty(passwordBaru) || TextUtils.isEmpty(passwordKonfirmasi)) {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah password baru memenuhi aturan
            if (!isValidPassword(passwordBaru)) {
                Toast.makeText(requireContext(), "Password baru tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordBaru != passwordKonfirmasi) {
                Toast.makeText(requireContext(), "Password baru dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek password lama dari Firebase
            ref.child(currentUserUsername).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserProfile::class.java)
                    if (user != null) {
                        if (user.password == passwordLama) {
                            // Jika password lama cocok, update password baru
                            ref.child(currentUserUsername).child("password").setValue(passwordBaru)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Gagal mengubah password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // Password lama tidak cocok
                            Toast.makeText(requireContext(), "Password lama tidak cocok", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Fungsi untuk validasi password baru
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 8 && hasUpperCase && hasDigit
    }

}
