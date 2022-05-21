package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.FragmentSettingsBinding
import ru.vladleesi.ultimatescanner.ui.activity.MainActivity
import ru.vladleesi.ultimatescanner.ui.activity.SettingsManager
import ru.vladleesi.ultimatescanner.ui.fragments.SettingsPreferenceFragment
import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment

class SettingsTabFragment : TabFragment(R.layout.fragment_settings) {

    private val binding by viewBinding(FragmentSettingsBinding::bind)

    private val mSettingsManager by lazy { parent<MainActivity>() as SettingsManager }

    fun getSettingsManager() = mSettingsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        if (savedInstanceState == null) openSettingsFragment()
    }

    private fun initToolbar() {
        binding.includeToolbar.tbToolbar.title = getString(R.string.tv_settings)
    }

    private fun openSettingsFragment() {
        childFragmentManager.beginTransaction()
            .add(
                binding.fcvMainContainer.id,
                SettingsPreferenceFragment.newInstance(),
                SettingsPreferenceFragment.TAG
            )
            .commit()
    }

    companion object {
        const val TAG = "SettingsFragment"

        fun newInstance() = SettingsTabFragment()
    }
}
