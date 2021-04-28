package com.rushil.voicerestaurant


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.databinding.CustomListBinding
import com.rushil.voicerestaurant.model.Items


class MainActivity : AppCompatActivity() {
    var itemRef = FirebaseDatabase.getInstance().getReference("items")
    var TAG = "main"
    private var rvItems: RecyclerView? = null
    private var addItem: FloatingActionButton? = null
    private val itemList: ArrayList<Items> = ArrayList()
    lateinit var adapter: LastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvItems = findViewById(R.id.rvItem)
        addItem = findViewById(R.id.addItem)
        rvItems!!.layoutManager = LinearLayoutManager(applicationContext)
        setAdapter()
        readData()



        addItem!!.setOnClickListener {
            // Create an alert builder
            // Create an alert builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Item")

            // set the custom layout

            // set the custom layout
            val customLayout: View = layoutInflater
                .inflate(
                    R.layout.custom_layout,
                    null
                )
            builder.setView(customLayout)

            builder
                .setPositiveButton(
                    "Add"
                ) { dialog, which -> // send data from the
                    val etname = customLayout.findViewById<EditText>(R.id.etName).text.toString()
                    val etprice = customLayout.findViewById<EditText>(R.id.etPrice).text.toString()

                    addItem(etname, etprice)
                }

            val dialog = builder.create()
            dialog.show()

        }

    }

    private fun addItem(name: String, price: String) {
        val Id: String = itemRef.push().getKey()!!
        Log.d("data=>", itemRef.toString())
        val items = Items(Id, name, price)
        itemRef.child(Id).setValue(items)
        Toast.makeText(applicationContext, "Item addes", Toast.LENGTH_LONG).show()
    }

    private fun readData() {
        Log.d(TAG, "Read Data")
        itemRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                itemList.clear()
                for (snapshot in dataSnapshot.children) {
                    val model = snapshot.value as Map<*, *>
                    Log.d(TAG, model.toString())

                    val price = model["price"].toString()
                    val name = model["name"].toString()
                    val id = model["id"].toString()

                    val user = Items(id, name, price)
                    Log.d(TAG, user.id)
                    itemList.add(user)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun setAdapter() {
        adapter =
            LastAdapter(itemList, BR.item).map<Items, CustomListBinding>(R.layout.custom_list) {
                onBind {
                    val position = it.adapterPosition
//                    it.binding.iName.text = (it.adapterPosition+1).toString()
//                    it.binding.iPrice.text=(it.adapterPosition).toString()
                    it.binding.iDel.setOnClickListener {
                        itemRef.child(itemList[position].id).removeValue()
                        Toast.makeText(this@MainActivity,itemList[position].name+" Remove",Toast.LENGTH_LONG).show()

                    }
                }
            }.into(rvItems!!)
    }

}