package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.example.project.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment
import android.widget.Toast
import android.content.SharedPreferences
import com.example.project.statistikPage.Statistik_Medis
import com.example.project.user.user_home
import com.example.project.user.user_profil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val Dashboard = Dashboard()
        val kamar = kamar()
        val profile = profile()
        val kamar_adm=kamar_adm()
        val Statistik_Medis = Statistik_Medis()
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        updateBottomNavigationMenu(isLoggedIn)
        if (isLoggedIn) {
            setCurrentFragment(Dashboard)
        } else {
            setCurrentFragment(user_home())
        }
        binding.bottomNavigationView.setItemBackgroundResource(R.color.colorAccent)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Dashboard -> {
                    if (isLoggedIn) {
                        setCurrentFragment(Dashboard)
                    } else {
                        setCurrentFragment(user_home())
                    }
                }
                R.id.kamar -> {
                    if (isLoggedIn) {
                        setCurrentFragment(kamar_adm)
                    } else {
                        setCurrentFragment(kamar)
                    }
                }
                R.id.medis -> setCurrentFragment(Statistik_Medis)
                R.id.profil -> {
                    val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
                    if (isLoggedIn) {
                        val username = sharedPreferences.getString("username", "Guest")
                        val nama = sharedPreferences.getString("nama", "Guest")
                        Toast.makeText(this, "Welcome back, $username!", Toast.LENGTH_SHORT).show()  // Menampilkan nama yang benar
                        setCurrentFragment(profile)
                    } else {
                        setCurrentFragment(user_profil())
                    }
                }
            }
            true
        }
    }
    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
                .addToBackStack(null)

            commit()
        }
    private fun updateBottomNavigationMenu(isLoggedIn: Boolean) {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView
        val menu = bottomNavigationView.menu

        if (isLoggedIn) {

            if (menu.findItem(R.id.medis) == null) {
                menu.add(0, R.id.medis, 2, "Medis").setIcon(R.drawable.ic_medis) // Adjust icon
            }
        } else {
            menu.removeItem(R.id.medis)
        }
    }
}
