package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.Data.Ruangan
import com.example.project.databinding.FragmentKamarBinding
import com.example.project.kamarPage.AturKamar
import com.google.firebase.database.*

class kamar : Fragment() {
    private lateinit var ref: DatabaseReference
    private lateinit var binding: FragmentKamarBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ref = FirebaseDatabase.getInstance().reference.child("ruangan")
        binding = FragmentKamarBinding.inflate(inflater, container, false)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val stringBuilder = StringBuilder()

                    for (dataSnapshot in snapshot.children) {
                        val ruangan = dataSnapshot.getValue(Ruangan::class.java)

                        if (ruangan != null) {
                            stringBuilder.append("Jenis: ${ruangan.jenis}\n")
                            stringBuilder.append("Status: ${ruangan.status}\n\n")
                        }
                    }
                    binding.listKamar.text = stringBuilder.toString()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })

        binding.buttonTambahPasien.setOnClickListener(){
            val AturKamar =AturKamar()
            setCurrentFragment(AturKamar)
        }
        binding.buttonRuangan.setOnClickListener(){
            val tambahKamar =AturKamar()
            setCurrentFragment(tambahKamar)
        }
        return binding.root

    }
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

}
