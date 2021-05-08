package com.rushil.voicerestaurant

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rushil.voicerestaurant.admin.AdminMainActivity
import com.rushil.voicerestaurant.model.UserModel
import com.rushil.voicerestaurant.user.UserMainActivity

class SplashActivity : AppCompatActivity() {

    var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        session= Session(this)
        Handler().postDelayed({
            if (session?.getuseId()?.isNotBlank()!!){
                if (session?.getuseId().equals("admin"))
                    startActivity(Intent(this, AdminMainActivity::class.java))
                else
                    startActivity(Intent(this, UserMainActivity::class.java))
            }else{
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1000)
    }
}