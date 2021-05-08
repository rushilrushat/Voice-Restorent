package com.rushil.voicerestaurant.admin

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.BR
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.databinding.AdminOrderListBinding
import com.rushil.voicerestaurant.model.OrderItemModel
import kotlinx.android.synthetic.main.admin_order_list.view.*


class Admin_orders_Fragment : Fragment() {
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "Admin_orders_Fragment"
    private var rvOrderItems: RecyclerView? = null

    lateinit var progressDialog: ProgressDialog
    private val orderList: ArrayList<OrderItemModel> = ArrayList()
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
        itemRef.getReference("order_items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressDialog.dismiss()
                orderList.clear()
                if (dataSnapshot.exists()) {
                    Log.e(TAG, dataSnapshot.toString())
                    for (snapshot in dataSnapshot.children) {
                        for (snap in snapshot.children){
                            val model =snap.getValue(OrderItemModel::class.java)
                            orderList.add(model!!)
                        }



                    }
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
            LastAdapter(orderList, BR.orderUI).map<OrderItemModel, AdminOrderListBinding>(R.layout.admin_order_list) {
                onBind {
                    val position = it.adapterPosition
                    if (it.binding.oStatus.text.equals(resources.getString(R.string.waitfordelivery))){
                        it.binding.oStatus.setTextColor(Color.RED)
                    }
                    if (it.binding.oStatus.text.equals(resources.getString(R.string.ourofdelivery))){
                        it.binding.oStatus.setTextColor(Color.BLUE)
                    }
                    if (it.binding.oStatus.text.equals(resources.getString(R.string.deliverd))){
                        it.binding.oStatus.setTextColor(Color.GREEN)
                    }
                    var button=it.binding.oDel
                    button.setOnClickListener {

                        val dropDownMenu = PopupMenu(context!!, button)
                        dropDownMenu.getMenuInflater().inflate(R.menu.popup_menu, dropDownMenu.getMenu())
                        dropDownMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->

                            itemRef.getReference("order_items").child(orderList[position].userId).child(orderList[position].o_id).child("status").setValue(item.title)
                            Toast.makeText(
                                context,
                                "Status Changed : " + orderList[position].userId+"\n"+orderList[position].o_id,
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        })
                        dropDownMenu.show()
//                        Toast.makeText(context,
//                            orderList[position].i_id + " Remove",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }
            }.into(rvOrderItems!!)
    }
}