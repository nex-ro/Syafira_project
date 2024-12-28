package com.example.project.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.databinding.FragmentUserProfilBinding
import android.content.Intent
import com.example.project.login

class user_profil : Fragment() {
    private lateinit var binding: FragmentUserProfilBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfilBinding.inflate(inflater, container, false)
        binding.loginbg.setOnClickListener {
            val intent = Intent(requireContext(), login::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}
