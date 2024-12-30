package com.example.project.kamarPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.Dashboard
import com.example.project.R
import com.example.project.Data.Ruangan
import com.example.project.databinding.FragmentKamarDetailBinding
import com.example.project.databinding.FragmentKamarJenisBinding
import com.example.project.user.user_home
import com.google.firebase.database.*
class KamarDetail : Fragment() {
    private lateinit var binding: FragmentKamarDetailBinding
    private lateinit var ref: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKamarDetailBinding.inflate(inflater, container, false)
        ref = FirebaseDatabase.getInstance().getReference("Ruangan")
        binding.backButton.setOnClickListener(){
            setCurrentFragment(user_home())
        }
        val idRuangan = arguments?.getString(ARG_ID)
        if (idRuangan != null) {
            fetchRuanganDetail(idRuangan)
        }

        return binding.root
    }

    private fun fetchRuanganDetail(id: String) {
        ref.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ruangan = snapshot.getValue(Ruangan::class.java)
                if (ruangan != null) {
                    binding.textViewNamaRuangan.text = ruangan.nama_Ruangan
                    binding.textViewNomorRuangan.text = "Nomor: ${ruangan.nomor_Ruangan}"
                    binding.textViewStatusRuangan.text = "Status: ${ruangan.status}"
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Log.e("KamarDetail", "Error fetching data: ${error.message}")
            }
        })
    }
    private fun setCurrentFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(id: String): KamarDetail {
            val fragment = KamarDetail()
            val args = Bundle().apply {
                putString(ARG_ID, id)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
