package com.rushil.voicerestaurant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.rushil.voicerestaurant.model.Items
import com.rushil.voicerestaurant.model.OrderItems
import java.util.*
import kotlin.collections.ArrayList

class VoiceCommand(activity: MainActivity) {
    var TAG = "VoiceCommand"
    var itemRef = FirebaseDatabase.getInstance().getReference("orders")
    var activity = activity
    lateinit var TtoS: TextToSpeech
    var MY_PERMISSIONS_RECORD_AUDIO = 1
    var itemList: ArrayList<Items> = ArrayList()
    init {
        TtoS = TextToSpeech(activity.applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                TtoS.setLanguage(Locale.getDefault())

            }
        }
    }

    fun speakOut(question: String) {

        TtoS.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(s: String) {}
            override fun onDone(s: String) {
                activity.runOnUiThread {
                    requestAudioPermissions()
                }
            }

            override fun onError(s: String) {}
        })
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        TtoS.speak(question, TextToSpeech.QUEUE_FLUSH, params, question)
    }

    fun requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Toast.makeText(
                    activity.applicationContext,
                    "Please grant permissions to record audio",
                    Toast.LENGTH_LONG
                )
                    .show()
                //Give user option to still opt-in the permissions
            } else {
                // Show user dialog to grant permission to record audio
            }
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_RECORD_AUDIO
            )
        } else if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechToText()
        }
    }

    fun startSpeechToText() {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity.applicationContext)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onError(error: Int) {
                val errorMessage: String = getErrorText(error)
                Log.d(TAG, "FAILED $errorMessage")
                Toast.makeText(activity.applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                speakOut(errorMessage)
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onResults(bundle: Bundle) {
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var text = ""
                for (result in matches!!) text = """$result""".trimIndent()
                Toast.makeText(activity.applicationContext,text,Toast.LENGTH_LONG).show()
                var words=text.split(" ")
                Log.d(TAG, words.toString())

                if (("buy" in words)){
                    var item=words[1]
                    var quintity=words[2].toInt()
                    for (i in itemList){
                        if (i.name.toUpperCase().contains(item.toUpperCase())){
                            orderItem(i.id,i.name,i.price,quintity)

                        }
                    }



                }

//                if(("show" in words)&&(("items" in words)||("item" in words))){
//
//                }else if (("add" in words)&&(("items" in words)||("item" in words))){
//                    speakOut("which you select")
//                }
//                var a=""
//                words.forEachIndexed { index, s -> if (itemList[index].name.equals(s)){
//                    Log.d(TAG, "${itemList[index].name} Added")
//                }  }

            }
            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    fun getErrorText(error: Int): String {
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
    private fun orderItem(i_id: String, name: String, price: Int, quintity: Int) {
        val oId: String = itemRef.push().getKey()!!
        Log.d("data=>", itemRef.toString())
        var totalprice:Int=price*quintity
        val oitems = OrderItems(oId, i_id, name, quintity,totalprice)
        itemRef.child(oId).setValue(oitems)
        speakOut("yous order is $oId, item is $name, quintity is $quintity, Total price is $totalprice, order registerd")

    }
}