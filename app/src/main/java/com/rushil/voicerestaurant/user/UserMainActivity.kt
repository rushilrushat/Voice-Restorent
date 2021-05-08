package com.rushil.voicerestaurant.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.rushil.voicerestaurant.LoginActivity
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.Session


class UserMainActivity : AppCompatActivity(){
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private var mic: FloatingActionButton? = null
    var TAG = "UserMainActivity"
    private var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        tabLayout = findViewById(R.id.tabLayoutUser)
        viewPager = findViewById(R.id.viewPagerUser)
        mic = findViewById(R.id.mic_command)
        tabLayout.addTab(tabLayout.newTab().setText("Items"))
        tabLayout.addTab(tabLayout.newTab().setText("Orders"))
        tabLayout.addTab(tabLayout.newTab().setText("Profile"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        session = Session(applicationContext)
        if (session?.getuseId()?.isBlank()!!) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        val adapter = UserAdapter(
            this, supportFragmentManager,
            tabLayout.tabCount
        )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
        })
//        val installIntent = Intent()
//        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
//        startActivity(installIntent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when(item.itemId){
            R.id.logout->{
                session?.setuseId("")
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        return true
    }
}