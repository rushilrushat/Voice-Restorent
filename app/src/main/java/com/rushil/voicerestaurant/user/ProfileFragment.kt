package com.rushil.voicerestaurant.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.rushil.voicerestaurant.LoginActivity
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.RegisterActivity
import com.rushil.voicerestaurant.Session
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class ProfileFragment : Fragment() {
    private var userRef = FirebaseDatabase.getInstance()
    private var session: Session? = null
    private var TAG = "ProfileFragment"
    lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = Session(context!!)
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()
        if (session?.getuseId()?.isBlank()!!) {
            startActivity(Intent(context, LoginActivity::class.java))
        }

        profileEdit.setOnClickListener {
            et_ProName.isEnabled = true
        }
        moNumberEdit.setOnClickListener {
            et_ProNum.isEnabled=true
        }
        addressEdit.setOnClickListener {
            et_Address.isEnabled=true
        }
        btnSave.setOnClickListener {
            if (et_ProName.isEnabled){
                userRef.getReference("users").child("${session?.getuseId()}").child("name")
                    .setValue(et_ProName.text.toString())
            }
            if (et_Address.isEnabled){
                userRef.getReference("users").child("${session?.getuseId()}").child("address")
                    .setValue(et_Address.text.toString())
            }
            if (et_ProNum.isEnabled){
                userRef.getReference("users").child("${session?.getuseId()}").child("phone")
                    .setValue(et_ProNum.text.toString())
            }
            Toast.makeText(this@ProfileFragment.context, "Save Profile", Toast.LENGTH_SHORT).show()
        }
        userRef.getReference("users").child("${session?.getuseId()}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val model = snapshot.value as Map<*, *>
                    et_Address.isEnabled = false
                    et_ProName.isEnabled = false
                    et_ProNum.isEnabled = false
                    et_Address.setText(model["address"].toString())
                    et_ProName.setText(model["name"].toString())
                    et_ProNum.setText(model["phone"].toString())
                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Log.w(TAG, "Failed to read value.", error.toException())
                }

            })
//        btnLogout.setOnClickListener {
//            session?.setuseId("")
//            startActivity(Intent(context, LoginActivity::class.java)
//            )
//
//        }
    }

}