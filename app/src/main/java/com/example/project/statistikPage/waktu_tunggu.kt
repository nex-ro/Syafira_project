package com.example.project.statistikPage

import com.google.firebase.database.DatabaseReference
import LaporanMedisAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Data.Laporan_Penanganan
import com.example.project.R
import com.example.project.databinding.FragmentLaporanMedisBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.project.databinding.FragmentWaktuTungguBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class waktu_tunggu : Fragment() {
    private var _binding: FragmentWaktuTungguBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaktuTungguBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simulasi fetch data dari database

    }

    private fun fetchAntrianData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Simulasi data dari database antrian
            val data = mapOf(
                "Radiologi" to "15 Menit",
                "Laboratorium" to "20 Menit",
                "Farmasi" to "25 Menit"
            )

          /*  withContext(Dispatchers.Main) {
                binding.textRadiologi.text = data["Radiologi"]
                binding.textLaboratorium.text = data["Laboratorium"]
                binding.textFarmasi.text = data["Farmasi"]
            }*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}