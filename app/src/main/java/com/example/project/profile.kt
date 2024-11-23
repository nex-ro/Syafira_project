package com.example.project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var ref: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        ref = FirebaseDatabase.getInstance().reference
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        binding.buttonLogout.setOnClickListener(){
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

        return binding.root
    }


}
