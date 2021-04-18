package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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

        binding.mbTestConnection.setOnClickListener {
            AnalyzeRepo(WeakReference(baseContext)).testConnection()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(baseContext, it, Toast.LENGTH_LONG).show()
                }, {
                    Toast.makeText(baseContext, it.message ?: it.toString(), Toast.LENGTH_LONG)
                        .show()
                })
        }
    }
}