package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import android.os.Bundle
import android.view.View
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.databinding.FragmentSettingsBinding
import ru.vladleesi.ultimatescanner.ui.activity.MainActivity
import ru.vladleesi.ultimatescanner.ui.activity.SettingsManager
import ru.vladleesi.ultimatescanner.ui.fragments.SettingsPreferenceFragment
import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment

class SettingsTabFragment : TabFragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    private val mSettingsManager by lazy { parent<MainActivity>() as SettingsManager }

    fun getSettingsManager() = mSettingsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettingsBinding.bind(view)

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

        fun newInstance() = SettingsTabFragment().apply { arguments = Bundle() }
    }
}
