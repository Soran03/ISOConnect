package com.example.fyp_coursework_test.Activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivitySplashBinding
import android.util.Pair as UtilPair


class SplashActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setAnimations()

            // Navigate to Sign In Activity after 3 seconds
            Handler().postDelayed({
                val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }, 3000)

        }
    }

    // Handle the animations of the logo
    private fun setAnimations() {
        val topAnim = AnimationUtils.loadAnimation(this, R.anim.fade_down_anim)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.fade_up_anim)
        val zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom)

        topAnim.startOffset = 1000
        bottomAnim.startOffset = 1000

        binding.bgStarIcon.animation = zoomAnim
        binding.splashLogoIcon.animation = topAnim
        binding.splashLogoTextIcon.animation = bottomAnim
    }
}