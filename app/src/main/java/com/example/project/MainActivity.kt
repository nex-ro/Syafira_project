package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.example.project.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val Dashboard = Dashboard()
        val kamar = kamar()
        val profile = profile()
        val Statistik_Medis = Statistik_Medis()


        setCurrentFragment(Dashboard)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Dashboard -> setCurrentFragment(Dashboard)
                R.id.kamar -> setCurrentFragment(kamar)
                R.id.medis -> setCurrentFragment(Statistik_Medis)
                R.id.profil -> setCurrentFragment(profile)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}
