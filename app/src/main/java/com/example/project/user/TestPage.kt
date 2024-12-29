package com.example.project.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentTestPageBinding

class TestPage : Fragment() {
    private lateinit var binding: FragmentTestPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestPageBinding.inflate(inflater, container, false)

        // Get data from arguments
        arguments?.let { args ->
            val namaPasien = args.getString("nama_pasien", "")
            val namaRuangan = args.getString("nama_ruangan", "")

            binding.textNamaPasien.text = namaPasien
            binding.textRuangan.text = namaRuangan
        }

        // Setup back button
//        binding.logoImageView.setOnClickListener {
//            parentFragmentManager.popBackStack()
//        }

        return binding.root
    }
}