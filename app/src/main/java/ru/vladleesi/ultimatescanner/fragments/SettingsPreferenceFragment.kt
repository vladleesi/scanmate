package ru.vladleesi.ultimatescanner.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ru.vladleesi.ultimatescanner.R

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
    }

    companion object {
        const val TAG = "SettingsPreferenceFragment"

        fun newInstance() = SettingsPreferenceFragment()
    }
}