package com.example.project.Profil

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.R
import com.example.project.databinding.FragmentDetailProfilBinding
import com.example.project.databinding.FragmentTambahProfileBinding
import com.example.project.profile

class Detail_Profile : Fragment(){


    private var _binding: FragmentDetailProfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout with ViewBinding
        _binding = FragmentDetailProfilBinding.inflate(inflater, container, false)

        // Load user data
        loadUserData()

        // Handle back button click to navigate to the profile fragment
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, profile()) // Replacing with the profile fragment
                .addToBackStack(null) // Optional: Add to back stack to allow back navigation
                .commit()
        }

        return binding.root
    }

    private fun loadUserData() {
        // Example: Loading data from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val username = sharedPreferences.getString("username", "N/A") // Mengambil username
        val fullName = sharedPreferences.getString("nama", " - ")
        val email = sharedPreferences.getString("email", " - ")
        val phone = sharedPreferences.getString("phone", " - ")
        val gender = sharedPreferences.getString("gender", " - ")

        // Load profile picture (assuming you have the URL or resource in shared preferences)
        val profilePicUrl = sharedPreferences.getString("profilePicUrl", "") // optional URL or resource

        // Set the data to the corresponding views
        binding.usernameInfo.text = "$username"
        binding.fullNameInfo.text = " $fullName"
        binding.infoEmailText.text = "$email"
        binding.mobileInfo.text = "$phone"
        binding.genderInfo.text = "$gender"

        // Set the greeting message with username
        binding.txtGreeting.text = "Hi, $username!" // Mengatur teks dengan username

        // Load profile picture if URL or resource is available
        if (profilePicUrl.isNullOrEmpty()) {
            // Set default profile image
            binding.profilePicture.setImageResource(R.drawable.ic_person) // default image resource
        } else {
            // Use an image loading library like Glide or Picasso if using URL
            // Glide.with(this).load(profilePicUrl).into(binding.profilePicture)
            // For this case, you might want to use a default image resource for now.
            binding.profilePicture.setImageResource(R.drawable.ic_person) // placeholder or default image
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}