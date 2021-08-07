package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.ActivitySettingsBinding
import ru.vladleesi.ultimatescanner.ui.fragments.SettingsPreferenceFragment

class SettingsActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        initToolbar()

        if (savedInstanceState == null) openSettingsFragment()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.includeToolbar.tbToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.includeToolbar.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.includeToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.includeToolbar.tbToolbar.title = getString(R.string.tv_settings)
    }

    private fun openSettingsFragment() {
        supportFragmentManager.beginTransaction()
            .add(
                binding.fcvMainContainer.id,
                SettingsPreferenceFragment.newInstance(),
                SettingsPreferenceFragment.TAG
            )
            .commit()
    }
}
