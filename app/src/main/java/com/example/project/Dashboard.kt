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
import com.example.project.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import com.example.project.admin.admin_history_kunjungan
import com.example.project.admin.admin_kelolah_kunjungan

class Dashboard : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    // Value event listeners
    private var rawatInapListener: ValueEventListener? = null
    private var rawatDaruratListener: ValueEventListener? = null
    private var visitorListener: ValueEventListener? = null
    private var statistikPasienListener: ValueEventListener? = null
    private var statistikKunjunganListener: ValueEventListener? = null

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
        setupInitialConfig()
        setupRealtimeListeners()
        setupClickListeners()
    }

    private fun setupInitialConfig() {
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
    }

    private fun setupClickListeners() {
        binding.historyKunjungan.setOnClickListener {
            setCurrentFragment(admin_history_kunjungan())
        }

        binding.kelolahKunjungan.setOnClickListener {
            setCurrentFragment(admin_kelolah_kunjungan())
        }
    }

    private fun setupRealtimeListeners() {
        val pasienRef = FirebaseDatabase.getInstance().getReference("pasien")
        val kunjunganRef = FirebaseDatabase.getInstance().getReference("Kunjungan")

        setupRawatInapListener(pasienRef)
        setupRawatDaruratListener(pasienRef)
        setupVisitorListener(kunjunganRef)
        setupStatistikListener()
    }

    private fun setupRawatInapListener(databaseRef: DatabaseReference) {
        rawatInapListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val count = snapshot.children.count {
                        it.child("status").getValue(String::class.java) == "Rawat Inap"
                    }
                    binding.rawatInapCount.text = count.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing rawat inap data: ${e.message}")
                    Toast.makeText(requireContext(), "Error mengambil data Rawat Inap", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Rawat inap listener cancelled: ${error.message}")
                Toast.makeText(requireContext(), "Gagal mengambil data Rawat Inap", Toast.LENGTH_SHORT).show()
            }
        }
        databaseRef.addValueEventListener(rawatInapListener!!)
    }

    private fun setupRawatDaruratListener(databaseRef: DatabaseReference) {
        rawatDaruratListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val count = snapshot.children.count {
                        it.child("status").getValue(String::class.java) == "Rawat Darurat"
                    }
                    binding.rawatDaruratCount.text = count.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing rawat darurat data: ${e.message}")
                    Toast.makeText(requireContext(), "Error mengambil data Rawat Darurat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Rawat darurat listener cancelled: ${error.message}")
                Toast.makeText(requireContext(), "Gagal mengambil data Rawat Darurat", Toast.LENGTH_SHORT).show()
            }
        }
        databaseRef.addValueEventListener(rawatDaruratListener!!)
    }

    private fun setupVisitorListener(databaseRef: DatabaseReference) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        visitorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val count = snapshot.children.count { child ->
                        val timestamp = child.child("tanggal_kunjungan").getValue(Long::class.java) ?: 0
                        val status = child.child("status").getValue(String::class.java)
                        timestamp in startOfDay..endOfDay && status == "Diterima"
                    }
                    binding.kunjunganCount.text = "$count orang"
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing visitor data: ${e.message}")
                    Toast.makeText(requireContext(), "Error mengambil data Kunjungan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Visitor listener cancelled: ${error.message}")
                Toast.makeText(requireContext(), "Gagal mengambil data Kunjungan", Toast.LENGTH_SHORT).show()
            }
        }
        databaseRef.addValueEventListener(visitorListener!!)
    }

    private fun setupStatistikListener() {
        // Setup Pasien Statistics
        statistikPasienListener = createStatistikListener(
            "Pasien",
            "tanggal_Masuk",
            binding.barChart1,
            "Jumlah Pasien per Hari"
        )
        FirebaseDatabase.getInstance().getReference("pasien")
            .addValueEventListener(statistikPasienListener!!)

        // Setup Kunjungan Statistics
        statistikKunjunganListener = createStatistikListener(
            "Kunjungan",
            "tanggal_kunjungan",
            binding.barChart2,
            "Jumlah Pengunjung per Hari"
        )
        FirebaseDatabase.getInstance().getReference("Kunjungan")
            .addValueEventListener(statistikKunjunganListener!!)
    }

    private fun createStatistikListener(
        jenisData: String,
        tanggalKey: String,
        namaChart: BarChart,
        labelData: String
    ): ValueEventListener {
        return object : ValueEventListener {
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
                                Log.e(TAG, "Error processing date: ${e.message}", e)
                            }
                        }
                    }

                    Log.d(TAG, "$jenisData Statistics by Date: $dataStatistik")
                    val dataList = dataStatistik.toList()
                    if (dataList.isNotEmpty()) {
                        setupBarChart(namaChart, dataList, labelData)
                    } else {
                        Log.w(TAG, "No $jenisData data found.")
                        Toast.makeText(requireContext(), "Tidak ada data $jenisData!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing statistik data: ${e.message}")
                    Toast.makeText(requireContext(), "Error memproses data statistik!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Statistik listener cancelled: ${error.message}")
                Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
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
        // Remove all listeners to prevent memory leaks
        rawatInapListener?.let {
            FirebaseDatabase.getInstance().getReference("pasien").removeEventListener(it)
        }
        rawatDaruratListener?.let {
            FirebaseDatabase.getInstance().getReference("pasien").removeEventListener(it)
        }
        visitorListener?.let {
            FirebaseDatabase.getInstance().getReference("Kunjungan").removeEventListener(it)
        }
        statistikPasienListener?.let {
            FirebaseDatabase.getInstance().getReference("pasien").removeEventListener(it)
        }
        statistikKunjunganListener?.let {
            FirebaseDatabase.getInstance().getReference("Kunjungan").removeEventListener(it)
        }
        _binding = null
    }
}