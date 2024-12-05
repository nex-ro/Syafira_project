package com.example.project.kamarPage
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.FragmentKamarJenisBinding
import com.google.firebase.database.*
import com.example.project.Data.Ruangan
import com.example.project.ItemAdapter
import com.example.project.R
import android.util.Log
import com.example.project.jenisAdapter
import kotlin.math.log


class kamarJenis : Fragment() {

    private lateinit var binding: FragmentKamarJenisBinding
    private lateinit var ref: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: jenisAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKamarJenisBinding.inflate(inflater, container, false)

        ref = FirebaseDatabase.getInstance().getReference("Ruangan")
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = jenisAdapter(listOf()) { id ->
            // Mengirimkan ID ke detailFragment
            val detailFragment = KamarDetail.newInstance(id)
            setCurrentFragment(detailFragment)
        }
        recyclerView.adapter = adapter

        val jenis = arguments?.getString(ARG_JENIS)
        binding.textViewJenis.text = "List Kamar Jenis $jenis"

        if (jenis != null) {
            fetchRuanganByJenis(jenis)
        }

        return binding.root
    }

    private fun fetchRuanganByJenis(jenis: String) {
        ref.orderByChild("jenis").equalTo(jenis)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dataRuangan = mutableListOf<Ruangan>()
                    for (ruanganSnapshot in snapshot.children) {
                        val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                        if (ruangan != null) {
                            dataRuangan.add(ruangan)
                        }
                    }
                    adapter.updateData(dataRuangan)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("kamarJenis", "Error fetching data: ${error.message}")
                }
            })
    }

    companion object {
        private const val ARG_JENIS = "jenis"

        fun newInstance(jenis: String): kamarJenis {
            val fragment = kamarJenis()
            val args = Bundle().apply {
                putString(ARG_JENIS, jenis)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }
}
