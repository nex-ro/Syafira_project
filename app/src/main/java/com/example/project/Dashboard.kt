package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class Dashboard : Fragment() {

    // Deklarasikan binding
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi ViewBinding
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Terapkan padding untuk sistem bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi BarChart untuk Rumah Sakit dan Unit Perawatan
        val barChart1: BarChart = binding.barChart1
        val barChart2: BarChart = binding.barChart2

        // Data Rumah Sakit
        val data1 = data() // Data dummy untuk Rumah Sakit
        setupBarChart(barChart1, data1)

        // Data Unit Perawatan
        val data2 = data2() // Data dummy untuk Unit Perawatan
        setupBarChart(barChart2, data2)
    }

    // Data dummy untuk Rumah Sakit
    private fun data(): List<Pair<String, Int>> {
        return listOf(
            "2024-01-01" to 20,
            "2024-01-02" to 15,
            "2024-01-03" to 30,
            "2024-01-04" to 25,
            "2024-01-05" to 20,
            "2024-01-06" to 30,
            "2024-01-07" to 15,
            "2024-01-08" to 10
        )
    }

    // Data dummy untuk Unit Perawatan
    private fun data2(): List<Pair<String, Int>> {
        return listOf(
            "2024-01-01" to 10,
            "2024-01-02" to 20,
            "2024-01-03" to 15,
            "2024-01-04" to 30,
            "2024-01-05" to 20,
            "2024-01-06" to 15,
            "2024-01-07" to 20,
            "2024-01-08" to 30
        )
    }

    private fun setupBarChart(barChart: BarChart, dailySales: List<Pair<String, Int>>) {
        // Ubah data ke format BarEntry
        val entries = dailySales.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.second.toFloat()) // X = indeks, Y = jumlah penjualan
        }

        // Buat DataSet untuk grafik
        val dataSet = BarDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.biru) // Warna batang
        dataSet.setDrawValues(false) // Sembunyikan nilai di atas batang
        dataSet.valueTextSize = 0f // Hapus nilai di atas batang

        // Siapkan BarData dan atur ke BarChart
        val barData = BarData(dataSet)
        barChart.data = barData

        // Konfigurasi sumbu X
        barChart.xAxis.apply {
            setDrawLabels(false)  // Sembunyikan label pada sumbu X
            setDrawGridLines(false)  // Sembunyikan garis grid pada sumbu X
            setDrawAxisLine(false)  // Sembunyikan garis sumbu X
        }

        // Konfigurasi sumbu Y
        barChart.axisRight.isEnabled = false // Sembunyikan garis sumbu Y sebelah kanan
        barChart.axisLeft.apply {
            setDrawLabels(false)  // Sembunyikan label pada sumbu Y
            setDrawGridLines(false)  // Sembunyikan garis grid pada sumbu Y
            setDrawAxisLine(false)  // Sembunyikan garis sumbu Y
        }

        // Nonaktifkan deskripsi
        barChart.description.isEnabled = false

        // Nonaktifkan interaksi
        barChart.setScaleEnabled(false) // Nonaktifkan zoom
        barChart.setTouchEnabled(false) // Nonaktifkan interaksi sentuh

        // Refresh tampilan grafik
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
