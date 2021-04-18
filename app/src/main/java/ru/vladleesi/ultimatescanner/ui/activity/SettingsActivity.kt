package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.databinding.ActivitySettingsBinding
import ru.vladleesi.ultimatescanner.ui.fragments.SettingsPreferenceFragment
import java.lang.ref.WeakReference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.tbToolbar.title = "Настройки"

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .add(
                    binding.fcvMainContainer.id,
                    SettingsPreferenceFragment.newInstance(),
                    SettingsPreferenceFragment.TAG
                )
                .commit()

        val handler = CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                Toast.makeText(
                    baseContext,
                    "ERROR: ${throwable.message ?: throwable.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.mbTestConnection.setOnClickListener {

            GlobalScope.launch(handler) {
                val message = AnalyzeRepo(WeakReference(applicationContext)).testConnectionAsync()
                lifecycleScope.launch {
                    Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}