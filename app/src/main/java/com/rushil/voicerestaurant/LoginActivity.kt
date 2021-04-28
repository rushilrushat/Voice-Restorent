package com.rushil.voicerestaurant

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var email = ""
    var password = ""
    val mAuth = FirebaseAuth.getInstance()
    private var dialog: ProgressDialog? = null
    var builder: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Please Wait....!")

        builder = AlertDialog.Builder(this)
        builder!!.setTitle(R.string.app_name).setCancelable(false)

        tvSignUp.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        })

        btnLogin.setOnClickListener {
            email = etEmail.text.toString()
            password = etPassword.text.toString()
            if (validate()) {
                dialog!!.show()
                mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                }.addOnFailureListener { e ->
                    dialog!!.dismiss()
                    Log.e("LoginActivity", "Exception : " + e.message)
                    builder!!.setMessage(e.message).setPositiveButton("OK",
                        DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    builder!!.show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun validate(): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmail.error = "Enter valid email"
        } else {
            tvEmail.error = ""
        }
        if (password.isBlank()) {
            tvPassword.error = "Enter password"
            return false
        } else {
            tvPassword.error = ""
        }
        return true
    }
}