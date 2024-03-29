package dev.vladleesi.scanmate.ui.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.ui.activity.Speecher
import java.lang.ref.WeakReference
import java.util.Locale

class VoiceMaker private constructor(weakContext: WeakReference<Context>) :
    TextToSpeech.OnInitListener, Speecher {

    private val tts = TextToSpeech(weakContext.get(), this)
    private val ttsParams = bundleOf(TextToSpeech.Engine.KEY_PARAM_VOLUME to 1f)

    private var isInitiated = false
    private var isEnable = false

    private var voiceInitListener: VoiceInitListener? = null

    init {
        weakContext.get()?.let {
            getDefaultSharedPreferences(it)?.let { prefs ->
                val prefsKey = weakContext.get()?.getString(R.string.settings_sound_maker)
                setEnable(prefs.getBoolean(prefsKey, false))
            }
        }
    }

    fun isTTSInitiated() = isInitiated

    fun setVoiceInitListener(voiceInitListener: VoiceInitListener) {
        this.voiceInitListener = voiceInitListener
    }

    fun setEnable(isEnable: Boolean) {
        this.isEnable = isEnable
    }

    override fun voice(text: String?) {
        if (isEnable)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams, "")
    }

    override fun stop() {
        stopWithChecking()
    }

    private fun stopWithChecking() {
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
        }
        Log.d(TAG, "TTS init: $isInitSuccess\n Status: $status")
        voiceInitListener?.onInitComplete(isInitSuccess)
        isInitiated = isInitSuccess
    }

    interface VoiceInitListener {
        fun onInitComplete(isInitSuccess: Boolean)
    }

    companion object {
        private const val TAG = "VoiceMaker"

        fun getInstance(context: Context) = VoiceMaker(WeakReference(context))
    }
}
