package com.rushil.voicerestaurant.admin

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.model.Items

class AdminMainActivity : AppCompatActivity() {
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private var addItem: FloatingActionButton? = null
    var dialog: AlertDialog? = null
    var itemRef = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        addItem = findViewById(R.id.addItem)

        tabLayout.addTab(tabLayout.newTab().setText("Items"))
        tabLayout.addTab(tabLayout.newTab().setText("Orders"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = MyAdapter(this, supportFragmentManager,
            tabLayout.tabCount)
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
        addItem!!.setOnClickListener {
            showAlert()
        }
    }
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Item")
        val customLayout: View = layoutInflater.inflate(R.layout.custom_layout, null)
        builder.setView(customLayout)

        builder.setPositiveButton("Add")
        { dialog, which -> // send data from the
            val etname = customLayout.findViewById<EditText>(R.id.etName).text.toString()
            val etprice =
                customLayout.findViewById<EditText>(R.id.etPrice).text.toString().toDouble()
            addItem(etname, etprice)
        }

        dialog = builder.create()
        dialog!!.show()

    }
    private fun addItem(name: String, price: Double) {
        val Id: String = itemRef.getReference("items").push().getKey()!!
        Log.d("data=>", itemRef.toString())
        val items = Items(Id, name, price)
        itemRef.getReference("items").child(Id).setValue(items)
        Toast.makeText(this, "Item addes", Toast.LENGTH_LONG).show()
    }
}