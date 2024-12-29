package com.example.project

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation

class Pop_up(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request a window without a title and set custom background
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.pop_up_login)

        // Set dialog width to wrap content
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Prevent dialog from being dismissed when clicking outside
        setCanceledOnTouchOutside(false)

        // Initialize views
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val btnKembali = findViewById<Button>(R.id.btnKembali)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Setup animations
        setupAnimations(closeButton)

        // Setup click listeners
        closeButton.setOnClickListener {
            dismiss()
        }

        btnKembali.setOnClickListener {
            dismiss()
        }

        btnLogin.setOnClickListener {
            // Start login activity
            context.startActivity(Intent(context, Login::class.java))
            dismiss()
        }
    }

    private fun setupAnimations(closeButton: ImageView) {
        try {
            // Translate Animation
            val translateAnimation = TranslateAnimation(0f, 0f, -20f, 20f).apply {
                duration = 1000
                repeatCount = TranslateAnimation.INFINITE
                repeatMode = TranslateAnimation.REVERSE
            }

            // Fade Animation
            val fadeAnimation = AlphaAnimation(0.5f, 1.0f).apply {
                duration = 1000
                repeatCount = AlphaAnimation.INFINITE
                repeatMode = AlphaAnimation.REVERSE
            }

            closeButton.startAnimation(translateAnimation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}