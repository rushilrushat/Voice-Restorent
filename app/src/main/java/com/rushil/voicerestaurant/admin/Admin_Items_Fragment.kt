package com.rushil.voicerestaurant.admin

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.databinding.CustomListBinding
import com.rushil.voicerestaurant.BR
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.model.Items


class Admin_Items_Fragment : Fragment() {
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "Admin_Items_Fragment"
    private var rvItems: RecyclerView? = null

    lateinit var progressDialog: ProgressDialog
    private val itemList: ArrayList<Items> = ArrayList()
    lateinit var adapter: LastAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvItems = view.findViewById(R.id.rvItem)
        rvItems!!.layoutManager = LinearLayoutManager(context)
        setAdapter()
        readData()
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin__items_, container, false)
    }


    private fun readData() {
        Log.d(TAG, "Read Data")
        itemRef.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressDialog.dismiss()
                itemList.clear()
                for (snapshot in dataSnapshot.children) {
                    val model = snapshot.value as Map<*, *>
                    val price = model["price"].toString().toDouble()
                    val name = model["name"].toString()
                    val id = model["id"].toString()

                    val user = Items(id, name, price)
                    itemList.add(user)
                }
                Log.d(TAG, itemList.toString())
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
            LastAdapter(itemList, BR.itemUI).map<Items, CustomListBinding>(R.layout.admin_item_list) {
                onBind {
                    val position = it.adapterPosition
                    it.binding.iDel.setOnClickListener {
                        itemRef.getReference("items").child(itemList[position].id).removeValue()
                        Toast.makeText(context,
                            itemList[position].name + " Remove",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.into(rvItems!!)
    }
}