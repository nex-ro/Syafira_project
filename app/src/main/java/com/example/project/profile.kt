    package com.example.project

<<<<<<< HEAD
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
=======
    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.MenuItem
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import com.bumptech.glide.Glide
    import com.example.project.Profil.Detail_Profile
    import com.example.project.Profil.EditProfil
    import com.example.project.Profil.TambahProfile
    import com.example.project.Profil.UbahPassword
    import com.example.project.databinding.FragmentProfileBinding
>>>>>>> f861ae1b275756983e336cde0a3cfb8822596817

    class profile : Fragment() {
        private var _binding: FragmentProfileBinding? = null
        private val binding get() = _binding!!
        private lateinit var sharedPreferences: SharedPreferences

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentProfileBinding.inflate(inflater, container, false)
            sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

            // Retrieve user session data from SharedPreferences
            val username = sharedPreferences.getString("username", "")
            val fullName = sharedPreferences.getString("nama", "")
            val email = sharedPreferences.getString("email", "")
            val profilePicUrl = sharedPreferences.getString("profilePicUrl", "")

            // Set toolbar title and user name
            binding.toolbarTitle.text = "Hi, $fullName"
            binding.username.text = "$username"
            binding.email.text = "$email"

            // Load profile picture (e.g., using Glide)
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profilePicUrl) // Assuming profilePicUrl is a URL or local path
                    .circleCrop() // Optional: crop image into a circle
                    .into(binding.profileImage)
            } else {
                // Set default image if no profile picture is available
                binding.profileImage.setImageResource(R.drawable.ic_person)
            }

            // Add click listeners for each item
            binding.itemDetailProfile.setOnClickListener {
                navigateToFragment(Detail_Profile())
            }

            binding.itemEditProfile.setOnClickListener {
                navigateToFragment(EditProfil())
            }

            binding.itemAddAccount.setOnClickListener {
                navigateToFragment(TambahProfile())
            }
            binding.itemChangePassword.setOnClickListener {
                navigateToFragment(UbahPassword())
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

        // Function to navigate to another fragment
        private fun navigateToFragment(fragment: Fragment) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .addToBackStack(null) // Add to backstack for easy navigation back
                .commit()
        }

        // Optionally handle logout here
        private fun logoutUser() {
            // Clear SharedPreferences
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }




        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

<<<<<<< HEAD
    private fun logoutUser() {
        // Clear SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, Login::class.java)
        startActivity(intent)
        requireActivity().finish()
=======
>>>>>>> f861ae1b275756983e336cde0a3cfb8822596817
    }
