package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import ru.vladleesi.ultimatescanner.databinding.ActivityMainBinding
import ru.vladleesi.ultimatescanner.extensions.addOnPageSelected
import ru.vladleesi.ultimatescanner.ui.accessibility.SoundMaker
import ru.vladleesi.ultimatescanner.ui.accessibility.VoiceMaker
import ru.vladleesi.ultimatescanner.ui.adapter.MainTabAdapter
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ResourceHolder {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var soundMaker: SoundMaker

    @Inject
    lateinit var voiceMaker: VoiceMaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // TODO:
        //  1. Камера не переинициализируется после уничтождения фрагмента
        //  2. При скроле истории уезжает тулбар
        //  3. Флаги доступности автодетекта и сигнала должны применятся моментально, через активити?
        //  4. Анимировать появление фрагмента только после инициализации камеры, найти нужны слушатель
        //  5. Рефакторинг + удалить струю навигацию

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())

        val tabAdapter = MainTabAdapter(supportFragmentManager, this)
        binding.fragmentContainer.adapter = tabAdapter
        binding.tabs.setupWithViewPager(binding.fragmentContainer)

        binding.fragmentContainer.addOnPageSelected {
            val fragmentTitle = tabAdapter.getPageTitle(position = it)
            voiceMaker.voice(fragmentTitle.toString())
            binding.root.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    override fun getStringRes(stringResId: Int): String = getString(stringResId)
}
