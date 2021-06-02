package com.rushil.voicerestaurant.user

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.LoginActivity
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.Session
import kotlinx.android.synthetic.main.activity_user_main.*
import kotlinx.android.synthetic.main.fragment_admin_time.view.*
import java.time.LocalTime


class UserMainActivity : AppCompatActivity(){
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private var mic: FloatingActionButton? = null
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "UserMainActivity"
    var oTime=""
    var cTime=""
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
        itemRef.reference.addValueEventListener(object :ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val model = snapshot.value as Map<*, *>
                oTime = model["oTime"].toString()
                cTime=model["cTime"].toString()
                var hp=model["oTime"].toString().split(":")[0].toInt()
                var f=""
                var min=model["oTime"].toString().split(":")[1]
                if (hp == 0) {
                    hp += 12;
                    f = "AM";
                } else if (hp == 12) {
                    f = "PM";
                } else if (hp > 12) {
                    hp -= 12;
                    f = "PM";
                } else {
                    f = "AM";
                }
                var o="$hp:$min $f"
                hp=model["cTime"].toString().split(":")[0].toInt()
                f=""
                min=model["cTime"].toString().split(":")[1]
                if (hp == 0) {
                    hp += 12;
                    f = "AM";
                } else if (hp == 12) {
                    f = "PM";
                } else if (hp > 12) {
                    hp -= 12;
                    f = "PM";
                } else {
                    f = "AM";
                }
                var c="$hp:$min $f"
                tvTime.text="Open Time $o  Close Time $c"
                var now= LocalTime.now()
                var open= LocalTime.of(oTime.split(":")[0].toInt(),oTime.split(":")[1].toInt())
                var close= LocalTime.of(cTime.split(":")[0].toInt(),cTime.split(":")[1].toInt())
                if(now.isAfter(open)&&now.isBefore(close)){
                    tvTime.setTextColor(Color.GREEN)
                    session!!.setStatus(true)
                }else{
                    session!!.setStatus(false)
                    tvTime.setTextColor(Color.RED)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                tvTime.text="Restorent Close"
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

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