package com.example.notedapp.login

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.notedapp.R
import com.example.notedapp.databinding.SplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding : SplashScreenBinding
    private val splashScreenTime = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = getSharedPreferences("settings", MODE_PRIVATE)

        if (sp.getBoolean("darkMode",
                this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)){
            val myEdit = sp.edit()
            myEdit.putBoolean("darkMode", true)
            myEdit.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else{
            val myEdit = sp.edit()
            myEdit.putBoolean("darkMode", false)
            myEdit.apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Animations
        val topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val botAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        // Hooks
        binding.logo.animation = topAnim
        binding.logoTXT.animation = botAnim

        // Go to next activity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OnBoarding::class.java)
            startActivity(intent)
            finish()
        }, splashScreenTime)
    }
}