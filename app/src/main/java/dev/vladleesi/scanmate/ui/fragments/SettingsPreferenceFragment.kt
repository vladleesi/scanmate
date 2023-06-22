package dev.vladleesi.scanmate.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import dagger.hilt.android.AndroidEntryPoint
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.ui.fragments.tabs.SettingsTabFragment
import dev.vladleesi.scanmate.ui.viewmodel.SettingsPreferenceViewModel

@AndroidEntryPoint
class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private val viewModel by viewModels<SettingsPreferenceViewModel>()

    private val settingsManager by lazy {
        (parentFragment as? SettingsTabFragment)?.getSettingsManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableOverScroll()
    }

    private fun disableOverScroll() {
        listView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        initPreferences()
    }

    private fun initPreferences() {

        findPreference<Preference>(getString(R.string.settings_test_connection))?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                viewModel.testConnection()
                true
            }
        }

        findPreference<SwitchPreference>(getString(R.string.settings_sound_maker))?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                settingsManager?.onSoundChanged(newValue == true)
                true
            }
        }
    }

    companion object {
        const val TAG = "SettingsPreferenceFragment"

        fun newInstance() = SettingsPreferenceFragment()
    }
}
