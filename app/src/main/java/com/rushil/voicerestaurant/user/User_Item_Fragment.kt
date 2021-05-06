package com.rushil.voicerestaurant.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.BR
import com.rushil.voicerestaurant.R
import com.rushil.voicerestaurant.databinding.UserItemListBinding
import com.rushil.voicerestaurant.model.Items
import com.rushil.voicerestaurant.model.OrderItemModel
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList


class User_Item_Fragment : Fragment(), TextToSpeech.OnInitListener {
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "User_Item_Fragment"
    private var rvItems: RecyclerView? = null
    lateinit var progressDialog: ProgressDialog
    private val itemList: ArrayList<Items> = ArrayList()
    lateinit var adapter: LastAdapter

    private var micButton: FloatingActionButton? = null
    lateinit var TtoS: TextToSpeech
    var question_which_item = "which item do you want to order."
    var question_Invalid_item = "invalid item"
    var question_quantity = "How much quantity do you want"
    var question_anything_else = "Do you want anything else"
    var question_order_successful = "your order place successful"
    var question = ""
    var MY_PERMISSIONS_RECORD_AUDIO = 1
    var recognizedText = ""

    lateinit var order: OrderItemModel
    var i_price: Double? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user__item_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvItems = view.findViewById(R.id.rvuItem)
        micButton = view.findViewById(R.id.mic_command)
        TtoS = TextToSpeech(this.context) { i ->
            if (i != TextToSpeech.ERROR) {
                TtoS.setLanguage(Locale.getDefault())

            }
        }
        order = OrderItemModel()
        rvItems!!.layoutManager = LinearLayoutManager(context)
        setAdapter()
        readData()
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()
        micButton!!.setOnClickListener {
            question = question_which_item
            speakOut(question)
        }
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
            LastAdapter(
                itemList,
                BR.itemUI
            ).map<Items, UserItemListBinding>(R.layout.user_item_list) {
                onBind {
                    val position = it.adapterPosition
                }
            }.into(rvItems!!)

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = TtoS.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                micButton!!.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    fun speakOut(question: String) {
        Log.d(TAG, "in speak->" + question)
        TtoS.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }

            override fun onDone(utteranceId: String?) {
                activity!!.runOnUiThread {
                    when (utteranceId) {
                        question_which_item -> vInput()
                        question_Invalid_item -> speakOut(question_which_item)
                        question_quantity -> vInput()
                        question_order_successful -> {
                            this@User_Item_Fragment.question = question_anything_else
                            speakOut(this@User_Item_Fragment.question)
                        }
                        question_anything_else -> vInput()
                    }
                }
            }

            override fun onError(utteranceId: String?) {

            }

        })
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        TtoS.speak(question, TextToSpeech.QUEUE_FLUSH, params, question)
        this.question = question

    }

    fun vInput() {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
        try {
            startActivityForResult(sttIntent, MY_PERMISSIONS_RECORD_AUDIO)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this.context, "Your device does not support STT.", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            MY_PERMISSIONS_RECORD_AUDIO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!result.isNullOrEmpty()) {
                        recognizedText = result[0]
                        when (question) {
                            question_which_item -> {
                                Log.d(TAG, recognizedText)
                                var s = false

                                for (i in itemList) {
                                    if (i.name.toUpperCase().equals(recognizedText.toUpperCase())) {
                                        s = true
                                        this.order.i_id = i.id
                                        this.order.itemName = i.name
                                        this.i_price = i.price
                                    }
                                }
                                if (s) {
                                    question = question_quantity
                                    speakOut(question)
                                } else {
                                    question = question_Invalid_item
                                    speakOut(question)

                                }
                            }
                            question_quantity -> {
                                Log.d(TAG, recognizedText.isDigitsOnly().toString())
                                if (recognizedText.toUpperCase().equals("TU")) {
                                    this.order.quantity = 2
                                    orderItem()
                                } else {
                                    try {
                                        var a = recognizedText.toIntOrNull()!!
                                        if (a != null) {
                                            this.order.quantity = recognizedText.toIntOrNull()!!
                                            orderItem()
                                        } else {
                                            this.question = question_quantity
                                            speakOut(question)
                                        }
                                    }catch (e:NullPointerException){
                                        Log.e(TAG,e.message.toString())
                                        this.question = question_quantity
                                        speakOut(question)

                                    }

                                }


                            }
                            question_anything_else -> {
                                if (recognizedText.equals("yes")) {
                                    this.question = question_which_item
                                    speakOut(this.question)
                                } else if (recognizedText.equals("no")) {
                                    activity!!.finish()
                                } else {
                                    speakOut(this.question)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun orderItem() {

        this.order.o_id = itemRef.getReference("order_items").push().getKey()!!
        this.order.status = "Wait For Delivery"
        this.order.totalPrice = this.order.quantity.times(this.i_price!!)
        Log.d(TAG, this.order.toString())
        itemRef.getReference("order_items").child(this.order.o_id.toString()).setValue(this.order)

        this.i_price = null
        this.order.i_id = null
        this.order.o_id = null
        this.order.status = null
        this.order.quantity = 0
        this.order.totalPrice = 0.0
        this.order.itemName = null
        this.question = question_order_successful
        speakOut(question)

    }
}