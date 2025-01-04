package com.example.project.statistikPage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.Data.WaktuTungguDisplay

import com.example.project.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class WaktuTungguChartFragment : Fragment() {

    private lateinit var chart: LineChart
    private val dataList = mutableListOf<WaktuTungguDisplay>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_waktu_tunggu_chart, container, false)
        chart = view.findViewById(R.id.lineChart)
        return view
    }

    fun updateChartData(data: List<WaktuTungguDisplay>) {
        dataList.clear()
        dataList.addAll(data)
        setupChart()
    }

    private fun setupChart() {
        val entries = dataList.mapIndexed { index, item ->
            Entry(index.toFloat(), item.waktuTunggu.toFloat())
        }

        val dataSet = LineDataSet(entries, "Waktu Tunggu")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK

        chart.data = LineData(dataSet)
        chart.invalidate()
    }
}
