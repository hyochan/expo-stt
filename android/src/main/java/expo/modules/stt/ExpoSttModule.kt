package expo.modules.stt

import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.content.Intent
import android.speech.RecognitionListener
import android.os.Bundle
import expo.modules.kotlin.exception.CodedException
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ExpoSttModule : Module(), RecognitionListener {
    private var isRecognizing: Boolean = false
    private var speech: SpeechRecognizer? = null;

    companion object {
        const val onSpeechStart = "onSpeechStart"
        const val onSpeechResult = "onSpeechResult"
        const val onPartialResults = "onPartialResults"
        const val onSpeechEnd = "onSpeechEnd"
        const val onSpeechError = "onSpeechError"
        const val TAG = "ExpoStt"
    }

    override fun definition() = ModuleDefinition {
        Name(TAG)

        AsyncFunction("requestRecognitionPermission") {
            requestRecognitionPermission()
        }

        AsyncFunction("checkRecognitionPermission") {
            return@AsyncFunction mapOf(
                "status" to "granted",
                "expires" to "never",
                "granted" to true,
                "canAskAgain" to true
            )
        }

        Function("startSpeech") {
            if (!isPermissionGranted()) {
                requestRecognitionPermission()
                return@Function false
            }

            if (isRecognizing) {
                sendEvent(onSpeechError, mapOf("cause" to "Speech recognition already started!"))
                return@Function false
            }
        
            if (speech != null) {
                speech?.destroy();
                speech = null;
            }
            
            CoroutineScope(Dispatchers.Main).launch {
                speech = SpeechRecognizer.createSpeechRecognizer(appContext.reactContext)
                speech?.setRecognitionListener(this@ExpoSttModule);
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                speech?.startListening(intent);
            }
            return@Function true
        }

        /**
         * There are nothing to do with these functions
         * since it belong to activity behavior
         * A bit different compare to iOS APIs
         */
        Function("stopSpeech") {
            if (speech != null) {
                speech?.stopListening();
            }
            isRecognizing = false;
            Log.d(TAG, "Stop Voice Recognizer")
        }

        Function("destroySpeech") {
            if (speech != null) {
                speech?.destroy();
            }
            isRecognizing = false;
            Log.d(TAG, "Destroy Voice Recognizer")
        }

        Events(onSpeechStart, onSpeechResult, onPartialResults, onSpeechEnd, onSpeechError)
    }

    private fun requestRecognitionPermission() {
        val currentActivity = appContext.currentActivity ?: throw CodedException("Activity is null")
        val permission = Manifest.permission.RECORD_AUDIO
        val isGranted = ContextCompat.checkSelfPermission(currentActivity, permission) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            ActivityCompat.requestPermissions(currentActivity, arrayOf(permission), 1)
        }

        mapOf(
                "status" to if (isGranted) "granted" else "denied",
                "expires" to "never",
                "granted" to isGranted,
                "canAskAgain" to true
        )
    }

    private fun isPermissionGranted(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val res = appContext.reactContext?.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        isRecognizing = true
        sendEvent(onSpeechStart)
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onResults(results: Bundle?) {
        isRecognizing = false
         val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        sendEvent(onSpeechResult, mapOf("results" to matches))
        Log.d(TAG, "onResults $matches")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(TAG, "onPartialResults $matches")
    }

    override fun onEndOfSpeech() {
        isRecognizing = false
        sendEvent(onSpeechEnd)
        Log.d(TAG, "onEndOfSpeech")
    }

    override fun onError(error: Int) {
        isRecognizing = false
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        
        sendEvent(onSpeechError, mapOf("errorMessage" to errorMessage))
        Log.d(TAG, "onError: $error $errorMessage")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(TAG, "onBufferReceived")
    }

    override fun onRmsChanged(rmsdB: Float) {
       Log.d(TAG, "onRmsChanged: $rmsdB")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent: $eventType")
    }
}
