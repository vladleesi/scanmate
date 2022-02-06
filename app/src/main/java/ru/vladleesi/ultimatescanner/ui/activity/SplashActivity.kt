package ru.vladleesi.ultimatescanner.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
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

        val anim = RotateAnimation(0f, 350f, 15f, 15f).apply {
            interpolator = LinearInterpolator()
            repeatCount = Animation.INFINITE
            duration = 700
        }

        findViewById<ImageView>(R.id.image_splash_logo).startAnimation(anim)

        if (voiceMaker.isTTSInitiated())
            closeActivity()
    }

    override fun onInitComplete(isInitSuccess: Boolean) = closeActivity()

    private fun closeActivity() {
        startActivity(Intent(baseContext, MainActivity::class.java))
        finish()
    }
}
