package com.example.project.statistikPage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.databinding.FragmentStatistikMedisBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Statistik_Medis : Fragment() {
    private var _binding: FragmentStatistikMedisBinding? = null
    private val binding get() = _binding!! // Ensure non-nullable usage
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatistikMedisBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference
        fetchRoomStatus()
        binding.cardStatusRuangan.setOnClickListener {
            setCurrentFragment(laporan_medis())
        }
        binding.cardWaktuTunggu.setOnClickListener {
            setCurrentFragment(waktu_tunggu())
        }
        setupFilterButtons()
        fetchPatientStatistics(TimeFilter.WEEK)
        // Fetch waktu tunggu
        fetchWaktuTunggu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding to avoid memory leaks
    }

    private fun fetchRoomStatus() {
        val statusList = listOf("VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium", "ICU", "HCU")

        database.child("Ruangan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (status in statusList) {
                        val matchingRooms = snapshot.children.filter {
                            it.child("jenis").value == status
                        }
                        matchingRooms.forEach { roomSnapshot ->
                            val roomStatus = roomSnapshot.child("status").value.toString()
                            val roomjenis = roomSnapshot.child("jenis").value.toString()

                            if (isAdded && _binding != null) { // Ensure fragment is still active
                                updateUI(roomjenis, roomStatus)
                            }
                        }
                    }
                } else {
                    Log.e("FirebaseDebug", "Data snapshot tidak ditemukan.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchWaktuTunggu() {
        database.child("history_antrian").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "Fetching data from history_antrian")

                if (snapshot.exists()) {
                    val waktuTungguMap = mutableMapOf<String, MutableList<Int>>()
                    snapshot.children.forEach { data ->
                        val kategori = data.child("jenis_ruangan").value.toString()
                        val waktuString = data.child("waktu_tunggu").value.toString().replace(" Menit", "")
                        val waktu = waktuString.toIntOrNull()


                        if (kategori.isNotBlank() && waktu != null) {
                            waktuTungguMap.putIfAbsent(kategori, mutableListOf())
                            waktuTungguMap[kategori]?.add(waktu)
                        } else {
                            Log.w("FirebaseDebug", "Invalid data found: Kategori=$kategori, Waktu=$waktu")
                        }
                    }

                    // Hitung rata-rata dan update UI
                    waktuTungguMap.forEach { (kategori, waktuList) ->
                        val rataRata = if (waktuList.isNotEmpty()) waktuList.average().toInt() else 0

                        updateWaktuTungguUI(kategori, rataRata)
                    }
                } else {
                    Log.e("FirebaseDebug", "Data history_antrian tidak ditemukan.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateWaktuTungguUI(kategori: String, rataRata: Int) {


        val waktuTextView: TextView? = when (kategori) {
            "Radiologi" -> binding.root.findViewById(R.id.textRadiologi)
            "Laboratorium" -> binding.root.findViewById(R.id.textLaboratorium)
            "Farmasi" -> binding.root.findViewById(R.id.textFarmasi)
            else -> null
        }

        if (waktuTextView != null) {
            waktuTextView.text = "$rataRata Menit"

        } else {
            Log.w("FirebaseDebug", "TextView not found for kategori: $kategori")
        }
    }

    private fun updateUI(roomjenis: String, roomStatus: String) {
        // Ensure _binding is not null to prevent crashes
        if (_binding == null) return

        val statusTextView: TextView? = when (roomjenis) {
            "ICU" -> binding.root.findViewById(R.id.texticustatus)
            "Laboratorium" -> binding.root.findViewById(R.id.textlaborstatus)
            "HCU" -> binding.root.findViewById(R.id.texthcustatus)
            else -> null
        }

        statusTextView?.let {
            when (roomStatus) {
                "terisi" -> {
                    it.text = "Terisi"
                    it.setTextColor(resources.getColor(R.color.terisi, null))
                }
                "penuh" -> {
                    it.text = "Penuh"
                    it.setTextColor(resources.getColor(R.color.penuh, null))
                }
                "kosong" -> {
                    it.text = "Kosong"
                    it.setTextColor(resources.getColor(R.color.kosong, null))
                }
                else -> {
                    it.text = "Unknown"
                    it.setTextColor(resources.getColor(R.color.unknown, null))
                }
            }
        }
    }

//    private fun setupBarChart(dataMap: Map<String, Int>) {
//        // Definisikan kategori pasien
//        val categories = listOf("Rawat Jalan", "Rawat Inap")
//
//        // Map kategori menjadi BarEntry
//        val entries = dataMap.map {
//            val index = categories.indexOf(it.key).toFloat() // Ambil indeks kategori
//            BarEntry(index, it.value.toFloat()) // Masukkan nilai ke BarEntry
//        }
//
//        // Set data pada grafik batang
//        val barDataSet = BarDataSet(entries, "Jumlah Pasien")
//        barDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Warna grafik
//        barDataSet.valueTextSize = 12f
//
//        val barData = BarData(barDataSet)
//        binding.root.findViewById<BarChart>(R.id.barChart).apply {
//            data = barData
//            description.text = "Status Pasien"
//            xAxis.apply {
//                granularity = 1f // Pastikan nilai X teratur
//                valueFormatter = IndexAxisValueFormatter(categories) // Tampilkan nama kategori
//            }
//            animateY(1000)
//            invalidate() // Refresh grafik
//        }
//    }

    private enum class TimeFilter {
        WEEK, MONTH, YEAR
    }

    private var currentFilter = TimeFilter.WEEK
    private fun setupFilterButtons() {
        binding.weekButton.apply {
            setOnClickListener {
                currentFilter = TimeFilter.WEEK
                updateFilterButtonsUI()
                fetchPatientStatistics(TimeFilter.WEEK)
            }
        }

        binding.monthButton.apply {
            setOnClickListener {
                currentFilter = TimeFilter.MONTH
                updateFilterButtonsUI()
                fetchPatientStatistics(TimeFilter.MONTH)
            }
        }

        binding.yearButton.apply {
            setOnClickListener {
                currentFilter = TimeFilter.YEAR
                updateFilterButtonsUI()
                fetchPatientStatistics(TimeFilter.YEAR)
            }
        }
    }

    private fun updateFilterButtonsUI() {
        // Update button states based on selected filter
        binding.weekButton.isSelected = currentFilter == TimeFilter.WEEK
        binding.monthButton.isSelected = currentFilter == TimeFilter.MONTH
        binding.yearButton.isSelected = currentFilter == TimeFilter.YEAR

        // Update button background colors
        val selectedColor = resources.getColor(R.color.ungu, null)
        val unselectedColor = resources.getColor(R.color.white, null)
        val selectedTextColor = resources.getColor(R.color.white, null)
        val unselectedTextColor = resources.getColor(R.color.ungu, null)

        binding.weekButton.apply {
            setBackgroundColor(if (isSelected) selectedColor else unselectedColor)
            setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
        }
        binding.monthButton.apply {
            setBackgroundColor(if (isSelected) selectedColor else unselectedColor)
            setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
        }
        binding.yearButton.apply {
            setBackgroundColor(if (isSelected) selectedColor else unselectedColor)
            setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
        }
    }

    private fun fetchPatientStatistics(filter: TimeFilter) {
        database.child("pasien").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataByDate = mutableMapOf<String, Int>()
                val calendar = Calendar.getInstance()

                // Set date range based on filter
                val endDate = calendar.timeInMillis
                when (filter) {
                    TimeFilter.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                    TimeFilter.MONTH -> calendar.add(Calendar.MONTH, -1)
                    TimeFilter.YEAR -> calendar.add(Calendar.YEAR, -1)
                }
                val startDate = calendar.timeInMillis

                // Initialize dates with 0
                initializeDateRange(dataByDate, startDate, endDate, filter)

                // Process patient data
                snapshot.children.forEach { patientSnapshot ->
                    val timestampStr = patientSnapshot.child("tanggal_Masuk").value.toString()
                    try {
                        val timestamp = timestampStr.toLong()
                        if (timestamp in startDate..endDate) {
                            val dateFormat = when (filter) {
                                TimeFilter.WEEK -> "dd/MM"
                                TimeFilter.MONTH -> "dd/MM"
                                TimeFilter.YEAR -> "MM/yyyy"
                            }
                            val date = SimpleDateFormat(dateFormat, Locale.getDefault())
                                .format(Date(timestamp))
                            dataByDate[date] = (dataByDate[date] ?: 0) + 1
                        }
                    } catch (e: NumberFormatException) {
                        Log.e("TimestampError", "Invalid timestamp: $timestampStr")
                    }
                }

                setupLineChart(dataByDate, filter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initializeDateRange(
        dataByDate: MutableMap<String, Int>,
        startDate: Long,
        endDate: Long,
        filter: TimeFilter
    ) {
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = endDate }

        val dateFormat = when (filter) {
            TimeFilter.WEEK -> "dd/MM"
            TimeFilter.MONTH -> "dd/MM"
            TimeFilter.YEAR -> "MM/yyyy"
        }

        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            dataByDate[formatter.format(calendar.time)] = 0
            when (filter) {
                TimeFilter.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                TimeFilter.MONTH -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                TimeFilter.YEAR -> calendar.add(Calendar.MONTH, 1)
            }
        }
    }

    private fun setupLineChart(dataByDate: Map<String, Int>, filter: TimeFilter) {
        val sortedData = dataByDate.toSortedMap()

        // Create entries for the chart
        val entries = sortedData.values.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "Jumlah Pasien").apply {
            color = resources.getColor(R.color.ungu, null)
            valueTextSize = 11f
            valueTextColor = resources.getColor(R.color.ungu, null)
            setCircleColor(resources.getColor(R.color.ungu, null))
            setDrawValues(true)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            mode = LineDataSet.Mode.LINEAR

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        val lineData = LineData(lineDataSet)

        binding.lineChart.apply {
            data = lineData

            description.apply {
                isEnabled = true
                text = when (filter) {
                    TimeFilter.WEEK -> "Statistik Pasien Mingguan"
                    TimeFilter.MONTH -> "Statistik Pasien Bulanan"
                    TimeFilter.YEAR -> "Statistik Pasien Tahunan"
                }
                textSize = 12f
                textColor = resources.getColor(R.color.ungu, null)
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(sortedData.keys.toList())
                labelRotationAngle = -45f
                textSize = 11f
                setDrawGridLines(false)

                // Adjust label count based on filter
                labelCount = when (filter) {
                    TimeFilter.WEEK -> 7
                    TimeFilter.MONTH -> 10
                    TimeFilter.YEAR -> 12
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textSize = 11f

                val maxValue = sortedData.values.maxOrNull()?.toFloat() ?: 0f
                val range = maxValue - 0f
                axisMinimum = 0f
                axisMaximum = maxValue + (range * 0.2f)

                setLabelCount(6, true)
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                textSize = 11f
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                setDrawInside(false)
            }

            setExtraOffsets(15f, 15f, 15f, 15f)
            animateXY(1000, 1000)

            invalidate()
        }
    }

    // Fungsi untuk mengonversi timestamp ke string tanggal
    private fun convertTimestampToDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return formatter.format(calendar.time)
    }

    private fun isWithinLastWeek(dateString: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = formatter.parse(dateString)
            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val lastWeek = calendar.time

            date != null && date.after(lastWeek) && date.before(today)
        } catch (e: ParseException) {
            false
        }
    }

//    private fun setupLineChart(dataMap: Map<String, Int>) {
//        val entries = dataMap.entries.mapIndexed { index, entry ->
//            Entry(index.toFloat(), entry.value.toFloat())
//        }
//
//        val lineDataSet = LineDataSet(entries, "Jumlah Pasien")
//        lineDataSet.color = ColorTemplate.COLORFUL_COLORS[0]
//        lineDataSet.valueTextSize = 10f
//        lineDataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[0])
//
//        val lineData = LineData(lineDataSet)
//
//        val lineChart = binding.root.findViewById<LineChart>(R.id.lineChart)
//        if (lineChart != null) {
//            lineChart.apply {
//                data = lineData
//                description.text = "Jumlah Pasien Selama 1 Minggu"
//                xAxis.apply {
//                    granularity = 1f
//                    valueFormatter = IndexAxisValueFormatter(dataMap.keys.toList())
//                }
//                animateX(1500)
//                invalidate()
//            }
//        } else {
//            Log.e("LineChartError", "LineChart view is null")
//        }
//    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
