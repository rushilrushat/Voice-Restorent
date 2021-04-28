package com.rushil.voicerestaurant


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rushil.voicerestaurant.databinding.CustomListBinding
import com.rushil.voicerestaurant.model.Items
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), RecognitionListener {
    var itemRef = FirebaseDatabase.getInstance().getReference("items")
    var TAG = "main"
    private var rvItems: RecyclerView? = null
    private var addItem: FloatingActionButton? = null
    private var micButton: FloatingActionButton? = null
    var dialog: AlertDialog? = null
    private val itemList: ArrayList<Items> = ArrayList()
    lateinit var adapter: LastAdapter
    lateinit var TtoS: TextToSpeech
    private val permission = 100
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvItems = findViewById(R.id.rvItem)
        addItem = findViewById(R.id.addItem)
        micButton = findViewById(R.id.micButton)

        TtoS=TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                TtoS.language = Locale.US
            }
        })
        rvItems!!.layoutManager = LinearLayoutManager(applicationContext)
        setAdapter()
        readData()
        voiceInit()
        micButton!!.setOnClickListener {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permission)
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Item")
        val customLayout: View = layoutInflater
            .inflate(
                R.layout.custom_layout,
                null
            )
        builder.setView(customLayout)

        builder
            .setPositiveButton(
                "Add"
            ) { dialog, which -> // send data from the
                val etname = customLayout.findViewById<EditText>(R.id.etName).text.toString()
                val etprice = customLayout.findViewById<EditText>(R.id.etPrice).text.toString()

                addItem(etname, etprice)
            }

        dialog = builder.create()

        addItem!!.setOnClickListener {

            dialog!!.show()

        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permission -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent)
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun voiceInit() {
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

    }

    private fun addItem(name: String, price: String) {
        val Id: String = itemRef.push().getKey()!!
        Log.d("data=>", itemRef.toString())
        val items = Items(Id, name, price)
        itemRef.child(Id).setValue(items)
        Toast.makeText(applicationContext, "Item addes", Toast.LENGTH_LONG).show()
    }

    private fun readData() {
        Log.d(TAG, "Read Data")
        itemRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun setAdapter() {
        adapter =
            LastAdapter(itemList, BR.item).map<Items, CustomListBinding>(R.layout.custom_list) {
                onBind {
                    val position = it.adapterPosition
                    it.binding.iDel.setOnClickListener {
                        itemRef.child(itemList[position].id).removeValue()
                        Toast.makeText(
                            this@MainActivity,
                            itemList[position].name + " Remove",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }.into(rvItems!!)
    }
    override fun onStop() {
        super.onStop()
        speech.destroy()
        TtoS.stop()
        Log.i(TAG, "destroy")
    }
    override fun onReadyForSpeech(params: Bundle?) {

    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onPartialResults(partialResults: Bundle?) {

    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech")

    }

    override fun onEndOfSpeech() {

    }

    override fun onError(error: Int) {
        val errorMessage: String = getErrorText(error)
        Log.d(TAG, "FAILED $errorMessage")
        Toast.makeText(this,errorMessage,Toast.LENGTH_LONG).show()
        TtoS.speak(errorMessage, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun getErrorText(error: Int): String {
        var message = ""
        message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        return message
    }

    override fun onResults(results: Bundle?) {
        val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in matches!!) text = """
      $result
      """.trimIndent()
        Toast.makeText(this,text,Toast.LENGTH_LONG).show()
        TtoS.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }
}