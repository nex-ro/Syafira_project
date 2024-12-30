package com.example.project.user

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project.databinding.FragmentUserProfilBinding
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.example.project.Login
import com.example.project.R
import androidx.core.content.ContextCompat
import com.example.project.Profil.EditProfil
import com.example.project.login.Register

class user_profil : Fragment() {
    private lateinit var binding: FragmentUserProfilBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.bg)
        binding = FragmentUserProfilBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val username = sharedPreferences.getString("username", "")
        val role = sharedPreferences.getString("role", "")

        if(isLoggedIn){
            binding.userName.text=username
            binding.userRole.text=role
            binding.editButton.text="Edit"
            binding.loginbg.text="Logout"
            binding.loginbg.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))

        }else{
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

        binding.loginbg.setOnClickListener {
           if(!isLoggedIn){
               val intent = Intent(requireContext(), Login::class.java)
               startActivity(intent)
           }else{
               logoutUser()
           }
        }
        binding.editButton.setOnClickListener(){
            if(!isLoggedIn){
                val intent = Intent(requireContext(), Register::class.java)
                startActivity(intent)
            }else{
                setCurrentFragment(EditProfil())
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
    private fun setCurrentFragment(fragment: Fragment) =
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
}
