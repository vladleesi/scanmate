package ru.vladleesi.ultimatescanner.ui.accessibility

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.ui.activity.Speecher
import java.lang.ref.WeakReference
import java.util.*

class VoiceMaker private constructor(weakContext: WeakReference<Context>) :
    TextToSpeech.OnInitListener, Speecher {

    private val tts = TextToSpeech(weakContext.get(), this)

    private var isEnable = false

    private val ttsParams by lazy {
        Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
        }
    }

    init {
        getDefaultSharedPreferences(weakContext.get())?.let { prefs ->
            val prefsKey = weakContext.get()?.getString(R.string.settings_sound_maker)
            setEnable(prefs.getBoolean(prefsKey, false))
        }
    }

    fun setEnable(isEnable: Boolean) {
        this.isEnable = isEnable
    }

    override fun voice(text: String?) {
        if (isEnable)
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
