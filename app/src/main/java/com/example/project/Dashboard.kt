package com.example.project

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.project.Data.Pasien
import com.example.project.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import com.example.project.admin.admin_history_kunjungan
import com.example.project.admin.admin_kelolah_kunjungan

class Dashboard : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    companion object {
        private const val TAG = "DashboardFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.bg)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        binding.toolbar.menu.findItem(R.id.quit)?.isVisible = isLoggedIn
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.quit -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        binding.historyKunjungan.setOnClickListener {
            setCurrentFragment(admin_history_kunjungan())
        }
        binding.kelolahKunjungan.setOnClickListener {
            setCurrentFragment(admin_kelolah_kunjungan())
        }

        database = FirebaseDatabase.getInstance().getReference("pasien")
        fetchStatistik("Pasien", "tanggal_Masuk", binding.barChart1, "Jumlah Pasien per Hari")

        database = FirebaseDatabase.getInstance().getReference("Kunjungan")
        fetchStatistik("Kunjungan", "tanggal_kunjungan", binding.barChart2, "Jumlah Pengunjung per Hari")
    }

    private fun fetchStatistik(
        jenisData: String,
        tanggalKey: String,
        namaChart: BarChart,
        labelData: String
    ) {
        try {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val dataStatistik = mutableMapOf<String, Int>()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        for (childSnapshot in snapshot.children) {
                            val tanggalLong = childSnapshot.child(tanggalKey).getValue(Long::class.java)
                            if (tanggalLong != null) {
                                try {
                                    val tanggal = Date(tanggalLong)
                                    val formattedDate = dateFormat.format(tanggal)
                                    dataStatistik[formattedDate] = dataStatistik.getOrDefault(formattedDate, 0) + 1
                                    Log.d(TAG, "$jenisData - Formatted Date: $formattedDate, Count: ${dataStatistik[formattedDate]}")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error saat memproses tanggal: ${e.message}", e)
                                }
                            } else {
                                Log.w(TAG, "$jenisData - Tanggal null atau tidak valid")
                            }
                        }

                        Log.d(TAG, "$jenisData Statistik per Tanggal: $dataStatistik")

                        val dataList = dataStatistik.map { it.key to it.value }
                        if (dataList.isNotEmpty()) {
                            setupBarChart(namaChart, dataList, labelData)
                        } else {
                            Log.w(TAG, "Tidak ada data $jenisData ditemukan.")
                            Toast.makeText(requireContext(), "Tidak ada data $jenisData!", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error saat memproses data: ${e.message}", e)
                        Toast.makeText(requireContext(), "Error saat memproses data!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database Error: ${error.message}", error.toException())
                    Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error saat mengambil data Firebase: ${e.message}", e)
            Toast.makeText(requireContext(), "Error saat mengambil data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBarChart(barChart: BarChart, dailyData: List<Pair<String, Int>>, labelData: String) {
        val entries = dailyData.mapIndexed { index, data ->
            BarEntry(index.toFloat(), data.second.toFloat())
        }

        val dataSet = BarDataSet(entries, labelData)
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.biru)
        dataSet.valueTextSize = 10f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData

        barChart.xAxis.apply {
            setDrawLabels(true)
            setDrawGridLines(false)
            setDrawAxisLine(false)
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    val dataSize = dailyData.size

                    return when (index) {
                        0 -> dailyData[index].first
                        dataSize - 1 -> dailyData[index].first
                        else -> ""
                    }
                }
            }
        }

        barChart.axisLeft.apply {
            axisLineWidth = 0f
            setDrawLabels(false)
            axisMinimum = 0f
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(false)
        barChart.legend.isEnabled = true

        barChart.invalidate()
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
