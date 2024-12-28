package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.databinding.FragmentBelumLoginBinding

class belum_login : Fragment() {
    private lateinit var binding: FragmentBelumLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBelumLoginBinding.inflate(inflater, container, false)
        binding.buttonlogin.setOnClickListener(){
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

        return binding.root
    }
}