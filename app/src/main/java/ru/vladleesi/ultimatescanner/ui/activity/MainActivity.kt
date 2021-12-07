package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.databinding.ActivityMainBinding
import ru.vladleesi.ultimatescanner.extensions.addOnPageSelected
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import ru.vladleesi.ultimatescanner.ui.adapter.InfinityMainTabAdapter
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraMode
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraModeHolder
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.TabFragments
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.VoiceEventBus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    ResourceHolder,
    SettingsManager {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var voiceMaker: VoiceMaker

    @Inject
    lateinit var soundMaker: SoundMaker

    private val tabAdapter by lazy { InfinityMainTabAdapter(supportFragmentManager, this) }

    private val coroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                showToast("ERROR: ${throwable.message ?: throwable.toString()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // TODO:
        //  - Анимировать появление фрагмента только после инициализации камеры, найти нужны слушатель
        //  - Рефакторинг + удалить старую навигацию
        //  - Утечка памяти на камере
        //  - TextToSpeech.OnInitListener вызывается поздно и озвучивает первый слайд с задержкой (мб инициализировать при старте активити?)
        //  - Автосвет
        //  - Анимация скольжения инликатора вкладок

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundleOf())

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

            when (val fragmentTitle = tabAdapter.getPageTitle(position = position)) {
                getString(TabFragments.HISTORY.titleResId) -> {
                    CameraModeHolder.cameraMode = CameraMode.MANUAL_MODE
                    CameraModeHolder.currentFragment = TabFragments.HISTORY
                    VoiceEventBus.toVoice(fragmentTitle.toString())
                }
                getString(TabFragments.SETTINGS.titleResId) -> {
                    CameraModeHolder.cameraMode = CameraMode.AUTO_MODE
                    CameraModeHolder.currentFragment = TabFragments.SETTINGS
                    VoiceEventBus.toVoice(fragmentTitle.toString())
                }
                getString(TabFragments.CAMERA.titleResId) -> {
                    CameraModeHolder.currentFragment = TabFragments.CAMERA
                    val title = when (CameraModeHolder.cameraMode) {
                        CameraMode.AUTO_MODE -> "Авто-детект"
                        CameraMode.MANUAL_MODE -> "Ручной режим"
                        CameraMode.UNDEFINED_MODE -> fragmentTitle.toString()
                    }
                    VoiceEventBus.toVoice(title)
                }
            }

            binding.root.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            binding.tabs.getTabAt(tabAdapter.getRealPosition(position))?.select()
        }

        CameraModeHolder.cameraMode = CameraMode.AUTO_MODE
        VoiceEventBus.toVoice("Авто-детект")

        GlobalScope.launch(coroutineExceptionHandler) {
            VoiceEventBus.flow.collect {
                voiceMaker.voice(it)
            }
        }
    }

    override fun getStringRes(stringResId: Int): String = getString(stringResId)

    override fun onSoundChanged(isSound: Boolean) {
        soundMaker.setEnable(isSound)
        voiceMaker.setEnable(isSound)
    }

    fun playSound() = soundMaker.playSound()
}
