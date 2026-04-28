package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        val anim = findViewById<LottieAnimationView>(R.id.lottieAnim)

        anim.setAnimation(R.raw.splash_animation)
        anim.playAnimation()
        anim.repeatCount = LottieDrawable.INFINITE

        Handler(Looper.getMainLooper()).postDelayed({

            if (auth.currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()

        }, 2500) // 2.5 seconds
    }
}