package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import ru.vladleesi.ultimatescanner.databinding.ActivityMainBinding
import ru.vladleesi.ultimatescanner.extensions.addOnPageSelected
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import ru.vladleesi.ultimatescanner.ui.adapter.InfinityMainTabAdapter
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraTabFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    ResourceHolder,
    SettingsManager,
    VoiceMaker.OnInitListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var voiceMaker: VoiceMaker

    @Inject
    lateinit var soundMaker: SoundMaker

    private val tabAdapter by lazy { InfinityMainTabAdapter(supportFragmentManager, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        voiceMaker.setOnInitListener(this)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())

        binding.fragmentContainer.adapter = tabAdapter
        binding.fragmentContainer.currentItem = tabAdapter.middle

        repeat(tabAdapter.getRealCount()) { index ->
            with(binding.tabs) {
                addTab(
                    newTab().apply {
                        text = tabAdapter.getPageTitle(index)
                    }
                )
            }
        }

        binding.fragmentContainer.addOnPageSelected(infinityScroll = false) { position ->
            val fragmentTitle = tabAdapter.getPageTitle(position = position)
            voiceMaker.voice(fragmentTitle.toString())
            binding.root.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            binding.tabs.getTabAt(tabAdapter.getRealPosition(position))?.select()
        }
    }

    override fun getStringRes(stringResId: Int): String = getString(stringResId)

    override fun onAutodetectChanged(isAutodetect: Boolean) {
        val cameraFragment = tabAdapter.getItem(0) as? CameraTabFragment
        cameraFragment?.setCameraDetectSettings(isAutodetect)
    }

    override fun onSoundChanged(isSound: Boolean) {
        soundMaker.setEnable(isSound)
        voiceMaker.setEnable(isSound)
    }

    override fun onInit(isSuccess: Boolean) {
        if (isSuccess) {
            // Voice first fragment
            tabAdapter.getPageTitle(0).toString().let { title -> voiceMaker.voice(title) }
        }
    }

    fun playSound() = soundMaker.playSound()
}
