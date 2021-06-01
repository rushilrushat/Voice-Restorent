package com.rushil.voicerestaurant.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.rushil.voicerestaurant.Session
import com.rushil.voicerestaurant.databinding.UserItemListBinding
import com.rushil.voicerestaurant.model.Items
import com.rushil.voicerestaurant.model.OrderItemModel
import kotlinx.android.synthetic.main.activity_user_main.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList


class User_Item_Fragment : Fragment(), TextToSpeech.OnInitListener {
    var itemRef = FirebaseDatabase.getInstance()
    var TAG = "User_Item_Fragment"
    private var rvItems: RecyclerView? = null
    lateinit var progressDialog: ProgressDialog
    private val itemList: ArrayList<Items> = ArrayList()
    private val orderList: ArrayList<OrderItemModel> = ArrayList()
    lateinit var adapter: LastAdapter
    private var session: Session? = null
    private var micButton: FloatingActionButton? = null
    lateinit var TtoS: TextToSpeech
    var question_default = "How may i help you."
    var question_which_item = "which item do you want to order."
    var question_Item_not_avilabale = "This item is not available for now."
    var question_quantity = "How much quantity do you want"
    var question_anything_else = "Do you want anything else"
    var question_order_successful = "your order place successful"
    var question_yes = "restaurant is Open"
    var question_no = "restaurant is Close"
    var question_restaurent_status = "what is restaurant Status"
    var question_buy_item = "i want to order item"
    var question_invalid_input = "invalid input"
    var question = ""
    var MY_PERMISSIONS_RECORD_AUDIO = 1
    var recognizedText = ""
    var itemId = ""
    var iName = ""
    var iPrice: Double = 0.0
    var iQty = 0
    var oTime=""
    var cTime=""
    var inputParser= SimpleDateFormat("HH:mm", Locale.US)
    lateinit var order: OrderItemModel

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
        session= Session(context!!)
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
            question = question_default
            speakOut(question)
        }
        itemRef.reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val model = snapshot.value as Map<*, *>
                oTime = model["oTime"].toString()
                cTime=model["cTime"].toString()
            }

            override fun onCancelled(error: DatabaseError) {
                tvTime.text="Restorent Close"
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
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
                        question_default -> vInput()
                        question_which_item -> vInput()
                        question_Item_not_avilabale -> speakOut(question_which_item)
                        question_quantity -> vInput()
                        question_order_successful -> {
//                            this@User_Item_Fragment.question = question_anything_else
//                            speakOut(this@User_Item_Fragment.question)
                            User_Order_Fragment()
                        }
                        question_anything_else -> vInput()
                        question_invalid_input -> speakOut(question_default)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                            question_default ->{
                                if (recognizedText.equals(question_restaurent_status,ignoreCase = true)){
                                    var now=LocalTime.now()
                                    var open=LocalTime.of(oTime.split(":")[0].toInt(),oTime.split(":")[1].toInt())
                                    var close=LocalTime.of(cTime.split(":")[0].toInt(),cTime.split(":")[1].toInt())
                                    if (now.isAfter(open)&&now.isBefore(close)){
                                        question=question_yes
                                        speakOut(question)
                                    }else{
                                        question=question_no
                                        speakOut(question)
                                    }


                                }else if (recognizedText.equals(question_buy_item,ignoreCase = true)){
                                    if (UserMainActivity().r_Status){
                                        question=question_which_item
                                        speakOut(question)
                                    }else{
                                        question=question_no
                                        speakOut(question)
                                    }

                                }else {
                                    question=question_invalid_input
                                    speakOut(question)
                                }
                            }
                            question_which_item -> {
                                Log.d(TAG, recognizedText)
                                var s = false
                                for (i in itemList) {
                                    if (i.name.equals(recognizedText, ignoreCase = true)) {
                                        s = true
                                        itemId = i.id
                                        iName = i.name
                                        iPrice = i.price
//                                        this.order.i_id = i.id
//                                        this.order.itemName = i.name
//                                        this.iPrice = i.price
//                                        return
                                    }
                                }
                                if (s) {
                                    question = question_quantity
                                    speakOut(question)
                                } else {
                                    question = question_Item_not_avilabale
                                    speakOut(question)

                                }
                            }
                            question_quantity -> {
                                Log.d(TAG, recognizedText.isDigitsOnly().toString())
                                if (recognizedText.equals("TU", true)) {
                                    iQty = 2
//                                    this.order.quantity = 2
                                    val id = itemRef.getReference("order_items").child(session?.getuseId().toString()).push().key
                                    val model =
                                        OrderItemModel(session?.getuseId(),id, itemId,R.string.waitfordelivery.toString(), iQty, (iQty * iPrice), iName)
                                    orderItem(model)
                                } else {
                                    try {
                                        val a = recognizedText.toIntOrNull()!!
                                        if (a != null) {
//                                            this.order.quantity = recognizedText.toIntOrNull()!!
                                            iQty = recognizedText.toIntOrNull()!!
                                            val id = itemRef.getReference("order_items").child(session?.getuseId().toString()).push().key
                                            val model = OrderItemModel(session?.getuseId(),id, itemId, resources.getString(R.string.waitfordelivery), iQty, (iQty * iPrice), iName)
                                            orderItem(model)
//                                            orderItem()
                                        } else {
                                            this.question = question_quantity
                                            speakOut(question)
                                        }
                                    } catch (e: NullPointerException) {
                                        Log.e(TAG, e.message.toString())
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
                                    for (o in orderList) {
                                        Log.d(TAG, o.itemName)
                                        itemRef.getReference("order_items").child(session?.getuseId().toString()).child(o.o_id).setValue(o)
                                    }
                                    orderList.clear()
                                    this.question = question_order_successful
                                    speakOut(question)
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

    private fun orderItem(model: OrderItemModel) {
//        this.order.o_id = itemRef.getReference("order_items").push().getKey()!!
//        this.order.status = "Wait For Delivery"
//        this.order.totalPrice = this.order.quantity.times(this.iPrice!!)
//        Log.d(TAG, this.order.toString())
        orderList.add(model)
//        this.order.i_id= null
//        this.order.o_id= null
//        this.order.status= null
//        this.order.quantity = 0
//        this.order.totalPrice = 0.0
//        this.order.itemName= null
//        this.iPrice=null

        this.question = question_anything_else
        speakOut(question)

    }

    private fun parseDate(date: String): Date? {
        return try {
            inputParser.parse(date)
        } catch (e: ParseException) {
            Date(0)
        }
    }
}