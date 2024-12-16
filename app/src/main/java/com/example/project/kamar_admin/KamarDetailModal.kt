package com.example.project.kamar_admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.project.Data.Ruangan
import com.example.project.R
import com.example.project.databinding.FragmentKamarBinding
import com.example.project.databinding.FragmentKamarDetailModalBinding

class KamarDetailModal : DialogFragment() {
    private lateinit var binding: FragmentKamarDetailModalBinding
    private lateinit var idRuangan: String
    private var nomorRuangan: Int = 0
    private lateinit var jenis: String
    private lateinit var namaRuangan: String
    private lateinit var status: String
    private var lantai: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idRuangan = it.getString("id_Ruangan", "")
            nomorRuangan = it.getInt("nomor_Ruangan", 0)
            jenis = it.getString("jenis", "")
            namaRuangan = it.getString("nama_Ruangan", "")
            status = it.getString("status", "")
            lantai = it.getInt("lantai", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
            binding = FragmentKamarDetailModalBinding.inflate(inflater, container, false)

        // Menampilkan data yang diterima

        binding.jenisKamarModal.text = jenis
        binding.nomorKamarModal.text = nomorRuangan.toString()

        return binding.root
    }

    companion object {
        fun newInstance(ruangan: Ruangan): KamarDetailModal {
            val fragment = KamarDetailModal()
            val args = Bundle().apply {
                putString("id_Ruangan", ruangan.id_Ruangan)
                putInt("nomor_Ruangan", ruangan.nomor_Ruangan ?: 0)
                putString("jenis", ruangan.jenis)
                putString("nama_Ruangan", ruangan.nama_Ruangan)
                putString("status", ruangan.status)
                putInt("lantai", ruangan.lantai ?: 0)
            }
            fragment.arguments = args
            return fragment
        }
    }

}
