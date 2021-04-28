package com.rushil.voicerestaurant

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rushil.voicerestaurant.model.UserModel

class SplashActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            Log.e("SplashActivity", "LoginUser : " + mAuth.currentUser)
            if (mAuth.currentUser != null) {
                val i = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(i)
                finish()
            } else {
                val i = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(i)
                finish()
            }
            finish()
        }, 1000)
    }
}