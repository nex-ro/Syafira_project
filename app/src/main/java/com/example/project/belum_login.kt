package com.example.project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.project.databinding.FragmentBelumLoginBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class belum_login : Fragment() {
    private lateinit var binding: FragmentBelumLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBelumLoginBinding.inflate(inflater, container, false)
        binding.buttonlogin.setOnClickListener(){
            val intent = Intent(activity, login::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

        return binding.root
    }
}