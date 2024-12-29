package com.example.project.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.R
import com.example.project.databinding.FragmentUserJanjiBinding
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.project.Login

class user_janji : Fragment() {
    private lateinit var binding: FragmentUserJanjiBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentUserJanjiBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val username = sharedPreferences.getString("username", "")
        if(isLoggedIn){

        }else{
            binding.empty.isVisible=true
            binding.toolbar.menu.findItem(R.id.quit)?.isVisible = isLoggedIn
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.quit -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        return binding.root
    }
    private fun logoutUser() {
        // Clear SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}