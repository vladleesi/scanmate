package ru.vladleesi.ultimatescanner.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import ru.vladleesi.ultimatescanner.Animations
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), VoiceMaker.VoiceInitListener {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var voiceMaker: VoiceMaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundleOf())

        voiceMaker.setVoiceInitListener(this)

        findViewById<ImageView>(R.id.image_splash_logo).startAnimation(Animations.ROTATE)

        if (voiceMaker.isTTSInitiated()) {
            closeActivity()
        }
    }

    override fun onInitComplete(isInitSuccess: Boolean) = closeActivity()

    private fun closeActivity() {
        startActivity(Intent(baseContext, MainActivity::class.java))
        finish()
    }
}
