package com.rushil.voicerestaurant


import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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


class MainActivity : AppCompatActivity(){
    var itemRef = FirebaseDatabase.getInstance().getReference("items")
    var TAG = "main"
    private var rvItems: RecyclerView? = null
    private var addItem: FloatingActionButton? = null
    private var micButton: FloatingActionButton? = null
    var dialog: AlertDialog? = null
    private val itemList: ArrayList<Items> = ArrayList()
    lateinit var adapter: LastAdapter
    lateinit var progressDialog: ProgressDialog
    lateinit var TtoS: TextToSpeech
    private lateinit var speech: SpeechRecognizer

    var defaultQuestion="Hello, I am you Waiter. What do you want to order?"
    lateinit var voiceCommand:VoiceCommand

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        voiceCommand=VoiceCommand(this)
        rvItems = findViewById(R.id.rvItem)
        addItem = findViewById(R.id.addItem)
        micButton = findViewById(R.id.micButton)
        rvItems!!.layoutManager = LinearLayoutManager(applicationContext)

        setAdapter()
        readData()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        micButton!!.setOnClickListener {
           voiceCommand.speakOut(defaultQuestion)

        }
        addItem!!.setOnClickListener {
            showAlert()
        }


    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Item")
        val customLayout: View = layoutInflater.inflate(R.layout.custom_layout, null)
        builder.setView(customLayout)

        builder.setPositiveButton("Add")
        { dialog, which -> // send data from the
            val etname = customLayout.findViewById<EditText>(R.id.etName).text.toString()
            val etprice = customLayout.findViewById<EditText>(R.id.etPrice).text.toString()
            addItem(etname, etprice)
        }

        dialog = builder.create()
        dialog!!.show()

    }

//    override fun onStop() {
//        super.onStop()
//        speech.destroy()
//        TtoS.stop()
//        Log.i(TAG, "destroy")
//    }
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
                progressDialog.dismiss()
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
                voiceCommand.itemList=itemList
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
            LastAdapter(itemList, BR.item).map<Items, CustomListBinding>(R.layout.custom_list) {
                onBind {
                    val position = it.adapterPosition
                    it.binding.iDel.setOnClickListener {
                        itemRef.child(itemList[position].id).removeValue()
                        Toast.makeText(this@MainActivity, itemList[position].name + " Remove", Toast.LENGTH_LONG).show()

                    }
                }
            }.into(rvItems!!)
    }

//
//    private fun getErrorText(error: Int): String {
//        var message = ""
//        message = when (error) {
//            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
//            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
//            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
//            SpeechRecognizer.ERROR_NETWORK -> "Network error"
//            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
//            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
//            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
//            SpeechRecognizer.ERROR_SERVER -> "error from server"
//            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
//            else -> "Didn't understand, please try again."
//        }
//        return message
//    }
//
//
//    private fun speakOut(question: String){
//        TtoS.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
//            override fun onStart(s: String) {}
//            override fun onDone(s: String) {
//                runOnUiThread {
//                    requestAudioPermissions()
//                }
//            }
//
//            override fun onError(s: String) {}
//        })
//        val params = Bundle()
//        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
//        TtoS.speak(question, TextToSpeech.QUEUE_FLUSH, params, question)
//        this.question = question
//    }
//    private fun requestAudioPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
//                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG)
//                    .show()
//                //Give user option to still opt-in the permissions
//            } else {
//                // Show user dialog to grant permission to record audio
//            }
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), MY_PERMISSIONS_RECORD_AUDIO)
//        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//            startSpeechToText()
//        }
//    }
//
//    private fun startSpeechToText() {
//        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        speechRecognizerIntent.putExtra(
//            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//        )
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//        speechRecognizer.setRecognitionListener(object : RecognitionListener {
//            override fun onReadyForSpeech(bundle: Bundle) {}
//            override fun onBeginningOfSpeech() {}
//            override fun onRmsChanged(v: Float) {}
//            override fun onBufferReceived(bytes: ByteArray) {}
//            override fun onEndOfSpeech() {}
//
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            override fun onError(error: Int) {
//                val errorMessage: String = getErrorText(error)
//                Log.d(TAG, "FAILED $errorMessage")
//                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
//                speakOut(errorMessage)
//            }
//
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            override fun onResults(bundle: Bundle) {
//                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                var text=""
//                for (result in matches!!) text = """$result""".trimIndent()
//                Answer(text)
////                Toast.makeText(applicationContext,text,Toast.LENGTH_LONG).show()
//            }
//
//            override fun onPartialResults(bundle: Bundle) {}
//            override fun onEvent(i: Int, bundle: Bundle) {}
//        })
//        speechRecognizer.startListening(speechRecognizerIntent)
//    }
//    fun Answer(text: String) {
//        if (answers.contains(text)){
//            showAlert()
//        }else{
//            Toast.makeText(applicationContext,"Error=>"+text,Toast.LENGTH_LONG).show()
//        }
//    }
}
