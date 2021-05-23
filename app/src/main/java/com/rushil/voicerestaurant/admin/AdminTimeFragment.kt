package com.rushil.voicerestaurant.admin

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.model.Items
import kotlinx.android.synthetic.main.activity_user_main.*
import kotlinx.android.synthetic.main.fragment_admin_time.view.*
import java.util.*


class AdminTimeFragment : Fragment() {
    var itemRef = FirebaseDatabase.getInstance()
    val mcurrentTime = Calendar.getInstance()
    lateinit var progressDialog: ProgressDialog
    private var TAG = "AdminTimeFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var hour = mcurrentTime[Calendar.HOUR_OF_DAY]
        var minutes = mcurrentTime[Calendar.MINUTE]
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()
        itemRef.reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val model = snapshot.value as Map<*, *>
                view.etOtime.setText(model["oTime"].toString())
                view.etCtime.setText(model["cTime"].toString())
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        view.etOtime.setOnClickListener {
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                context,
                { timePicker, selectedHour, selectedMinute ->
                    var format=""
                    var hp=selectedHour
                    if (hp == 0) {
                        hp += 12;
                        format = "AM";
                    } else if (hp == 12) {
                        format = "PM";
                    } else if (hp > 12) {
                        hp -= 12;
                        format = "PM";
                    } else {
                        format = "AM";
                    }

                    view.etOtime.setText("$hp:$selectedMinute $format")
                },
                hour,
                minutes,
                false
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
        view.etCtime.setOnClickListener {

            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                context,
                { timePicker, selectedHour, selectedMinute ->
                    var format=""
                    var hp=selectedHour
                    if (hp == 0) {
                        hp += 12;
                        format = "AM";
                    } else if (hp == 12) {
                        format = "PM";
                    } else if (hp > 12) {
                        hp -= 12;
                        format = "PM";
                    } else {
                        format = "AM";
                    }

                    view.etCtime.setText("$hp:$selectedMinute $format")
                },
                hour,
                minutes,
                false
            ) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()

        }
        view.btnSubmit.setOnClickListener {
            if (validate()) {
                itemRef.reference.child("oTime").setValue(view.etOtime.text.toString())
                itemRef.reference.child("cTime").setValue(view.etCtime.text.toString())
//                Toast.makeText(context, "Time Set", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun validate(): Boolean {

        if (view!!.etOtime.text.isNullOrBlank()) {
            view!!.etOtime.error = "Select Open Time"
            return false
        }else if(view!!.etCtime.text.isNullOrBlank()) {
            view!!.etCtime.error = "Select Close Time"
            return false
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_time, container, false)
    }

}