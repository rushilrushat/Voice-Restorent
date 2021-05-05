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
import com.rushil.voicerestaurant.BR
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.databinding.CustomListBinding
import com.rushil.voicerestaurant.model.Items
import com.rushil.voicerestaurant.model.OrderItems
import kotlinx.android.synthetic.main.admin_order_list.view.*

class Admin_orders_Fragment : Fragment() {
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "Admin_orders_Fragment"
    private var rvOrderItems: RecyclerView? = null

    lateinit var progressDialog: ProgressDialog
    private val orderList: ArrayList<OrderItems> = ArrayList()
    lateinit var adapter: LastAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_orders_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvOrderItems = view.findViewById(R.id.rvOrderItem)
        rvOrderItems!!.layoutManager = LinearLayoutManager(context)
        setAdapter()
        readData()
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }
    private fun readData() {
        Log.d(TAG, "Read Data")
        itemRef.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressDialog.dismiss()
                orderList.clear()
                for (snapshot in dataSnapshot.children) {
                    val model = snapshot.value as Map<*, *>
                    val o_id = model["id"].toString()
                    val i_id = model["i_id"].toString()
                    val quintity = model["quintity"].toString().toInt()
                    val totalPrice = model["totalPrice"].toString().toDouble()
                    val status = model["status"].toString()

                    val order = OrderItems(o_id, i_id, quintity,totalPrice,status)
                    orderList.add(order)
                }
                Log.d(TAG, orderList.toString())
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
            LastAdapter(orderList, BR.orderUI).map<OrderItems, CustomListBinding>(R.layout.admin_order_list) {
                onBind {
                    val position = it.adapterPosition
                    it.binding.oDel.setOnClickListener {
//                        itemRef.getReference("items").child(orderList[position].o_id).removeValue()
                        Toast.makeText(context,
                            orderList[position].i_id + " Remove",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.into(rvOrderItems!!)
    }
}