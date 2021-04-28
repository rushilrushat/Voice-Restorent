package com.rushil.voicerestaurant


import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvItems = findViewById(R.id.rvItem)
        addItem = findViewById(R.id.addItem)
        rvItems!!.layoutManager = LinearLayoutManager(applicationContext)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        setAdapter()
        readData()

        addItem!!.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Item")
            builder.setCancelable(false)

            val customLayout: View = layoutInflater
                .inflate(
                    R.layout.custom_layout,
                    null
                )
            builder.setView(customLayout)

            val btnAdd = customLayout.findViewById<Button>(R.id.btnAdd)
            val btnCancel = customLayout.findViewById<Button>(R.id.btnCancel)
            val etPrice = customLayout.findViewById<TextInputEditText>(R.id.etPrice)
            val etName = customLayout.findViewById<TextInputEditText>(R.id.etName)

            val tvItem = customLayout.findViewById<TextInputLayout>(R.id.tvItem)
            val tvPrice = customLayout.findViewById<TextInputLayout>(R.id.tvPrice)

            val dialog = builder.create()
            dialog.show()

            btnAdd.setOnClickListener {
                val item = etName.text.toString()
                val price = etPrice.text.toString()
                if (validate(item, price, tvItem, tvPrice)) {
                    val id: String = itemRef.push().key!!
                    Log.d("data=>", itemRef.toString())
                    val items = Items(id, item, price)
                    itemRef.child(id).setValue(items)
                    Toast.makeText(applicationContext, "Item Added", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

        }

    }

    private fun validate(
        item: String,
        price: String,
        tvItem: TextInputLayout,
        tvPrice: TextInputLayout
    ): Boolean {
        if (item.isBlank()) {
            tvItem.error = "Enter item name"
            return false
        } else {
            tvItem.error = ""
        }
        if (price.isBlank()) {
            tvPrice.error = "Enter item price"
            return false
        } else {
            tvPrice.error = ""
        }
        return true
    }

    private fun readData() {
        Log.d(TAG, "Read Data")
        itemRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressDialog.dismiss()
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
                progressDialog.dismiss()
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