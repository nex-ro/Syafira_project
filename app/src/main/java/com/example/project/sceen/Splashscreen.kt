package com.example.project.sceen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.project.MainActivity
import com.example.project.R

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        // Membuat splash screen menjadi full screen dengan menyembunyikan status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Menggunakan Handler untuk menunda perpindahan ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Intent untuk pindah ke MainActivity
            val intent = Intent(this@Splashscreen, MainActivity::class.java)
            startActivity(intent)
            finish() // Menutup Splashscreen agar tidak bisa diakses kembali
        }, 3000) // Delay selama 3 detik
    }
}
