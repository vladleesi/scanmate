package ru.vladleesi.ultimatescanner.ui.accessibility

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import ru.vladleesi.ultimatescanner.ui.activity.Speecher
import java.lang.ref.WeakReference
import java.util.*

class VoiceMaker private constructor(weakContext: WeakReference<Context>) :
    TextToSpeech.OnInitListener, Speecher {

    private val tts = TextToSpeech(weakContext.get(), this)

    private val ttsParams by lazy {
        Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
        }
    }

    override fun voice(text: String?) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams, "")
    }

    override fun stop() {
        tts.stopWithChecking()
    }

    private fun TextToSpeech.stopWithChecking() {
        if (tts.isSpeaking) tts.stop()
    }

    override fun onInit(status: Int) {
        var isInitSuccess = false
        if (status == TextToSpeech.SUCCESS) {
            if (tts.isLanguageAvailable(Locale(Locale.getDefault().language))
                == TextToSpeech.LANG_AVAILABLE
            ) {
                tts.language = Locale(Locale.getDefault().language)
            } else {
                tts.language = Locale.US
            }
            tts.setPitch(1.3f)
            tts.setSpeechRate(0.9f)
            isInitSuccess = true
        } else if (status == TextToSpeech.ERROR) {
        }
        Log.d(TAG, "TTS init: $isInitSuccess\n Status: $status")
    }

    companion object {
        private const val TAG = "VoiceMaker"

        fun getInstance(context: Context) = VoiceMaker(WeakReference(context))
    }
}
