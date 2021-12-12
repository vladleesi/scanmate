package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.ActivityMainBinding
import ru.vladleesi.ultimatescanner.extensions.addOnPageSelected
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import ru.vladleesi.ultimatescanner.ui.adapter.InfinityMainTabAdapter
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.*
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

    val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(baseContext) }

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

        repeat(tabAdapter.getRealCount() + 1) { index ->
            with(binding.tabs) {
                addTab(
                    newTab().apply {
                        text = getString(TabFragmentFactory.getTabTitle(index))
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
                        CameraMode.AUTO_MODE -> getString(R.string.page_title_auto_detect)
                        CameraMode.MANUAL_MODE -> getString(R.string.page_title_manual)
                        CameraMode.UNDEFINED_MODE -> fragmentTitle.toString()
                    }
                    VoiceEventBus.toVoice(title)
                }
            }

            binding.root.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            selectTab(tabAdapter.getRealPosition(position))
        }

        CameraModeHolder.cameraMode = CameraMode.AUTO_MODE
        VoiceEventBus.toVoice("Авто-детект")

        GlobalScope.launch(coroutineExceptionHandler) {
            VoiceEventBus.flow.collect {
                voiceMaker.voice(it)
            }
        }
    }

    fun selectTab(tabPosition: Int, fromCamera: Boolean = false) {
        val realTabPosition = when (tabPosition) {
            0 -> when (CameraModeHolder.cameraMode) {
                CameraMode.AUTO_MODE -> 0
                CameraMode.MANUAL_MODE -> 1
                CameraMode.UNDEFINED_MODE -> tabPosition
            }
            else -> tabPosition + 1
        }
        binding.tabs.getTabAt(if (fromCamera) tabPosition else realTabPosition)?.select()
    }

    override fun getStringRes(stringResId: Int): String = getString(stringResId)

    override fun onSoundChanged(isSound: Boolean) {
        soundMaker.setEnable(isSound)
        voiceMaker.setEnable(isSound)
    }

    fun playSound() = soundMaker.playSound()
}
