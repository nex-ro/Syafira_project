package com.example.project.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.Dashboard
import com.example.project.Data.DateItem
import com.example.project.R
import com.example.project.admin.adapter.TanggalAdapter
import com.example.project.databinding.FragmentAdminKelolahKunjunganBinding
import java.text.SimpleDateFormat
import java.util.*

class admin_kelolah_kunjungan : Fragment() {
    private lateinit var binding: FragmentAdminKelolahKunjunganBinding
    private lateinit var tanggalAdapter: TanggalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminKelolahKunjunganBinding.inflate(inflater, container, false)
        setupViews()
        return binding.root
    }

    private fun setupViews() {
        setupBackButton()
        setupRecyclerView()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            setCurrentFragment(Dashboard())
        }
    }

    private fun setupRecyclerView() {
        tanggalAdapter = TanggalAdapter(generateDateList(), ::onDateSelected)
        binding.recyclerViewTanggal.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tanggalAdapter
        }
    }

    private fun generateDateList(): List<DateItem> {
        val dateList = mutableListOf<DateItem>()
        val calendar = Calendar.getInstance()

        // Generate dates for next 30 days
        repeat(30) {
            dateList.add(
                DateItem(
                    date = calendar.time,
                    isSelected = it == 0 // First day is selected by default
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dateList
    }

    private fun onDateSelected(position: Int) {
        tanggalAdapter.updateSelectedPosition(position)
        // Here you can handle what happens when a date is selected
        // For example, load data for that specific date
    }

    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}

