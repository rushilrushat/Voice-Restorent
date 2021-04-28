package com.rushil.voicerestaurant

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rushil.voicerestaurant.model.UserModel
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    var db = FirebaseDatabase.getInstance()

    var name = ""
    var address = ""
    var email = ""
    var password = ""
    var phone = ""
    private var dialog: ProgressDialog? = null
    var builder: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Please Wait....!")
        dialog!!.setCancelable(false)

        builder = AlertDialog.Builder(this)
        builder!!.setTitle(R.string.app_name).setCancelable(false)

        ibBack.setOnClickListener(View.OnClickListener { onBackPressed() })

        btnRegister.setOnClickListener {
            name = etName.text.toString()
            email = etEmail.text.toString()
            address = etAddress.text.toString()
            password = etPassword.text.toString()
            phone = etPhone.text.toString()

            if (validate()) {
                dialog!!.show()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Registration successful!",
                                Toast.LENGTH_LONG
                            ).show()
                            task.addOnSuccessListener { authResult ->
                                if (!Objects.requireNonNull(authResult.user).isEmailVerified) authResult.user.sendEmailVerification()
                                val id = authResult.user.uid
                                val model = UserModel(id, name, email, address, phone)

                                db.getReference(Constant.USER_COLLECTION).child(id)
                                    .setValue(model)

                                dialog!!.dismiss()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Registration failed! Please try again later",
                                Toast.LENGTH_LONG
                            ).show()
                            dialog!!.dismiss()
                        }
                        task.addOnFailureListener { e ->
                            Log.e("RegisterActivity", "OnFail : " + e.message)
                            builder!!.setMessage(e.message).setPositiveButton(
                                "OK"
                            ) { dialog, which -> dialog.dismiss() }
                            builder!!.show()
                            e.printStackTrace()
                        }
                    }
            }
        }
    }

    private fun validate(): Boolean {
        if (name.isBlank()) {
            tvName.error = "Enter Name"
        } else {
            tvName.error = ""
        }
        if (address.isBlank()) {
            tvAddress.error = "Enter Address"
        } else {
            tvAddress.error = ""
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmail.error = "Enter valid email"
        } else {
            tvEmail.error = ""
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            tvPhone.error = "Enter valid phone number"
        } else {
            tvPhone.error = ""
        }
        if (password.isBlank()) {
            tvPassword.error = "Enter Password"
        } else {
            tvPassword.error = ""
        }
        if (password.length < 6) {
            tvPassword.error = "Password length should be 6"
        } else {
            tvPassword.error = ""
        }
        return true
    }
}