package ru.vladleesi.ultimatescanner.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.extensions.showToast
import java.lang.ref.WeakReference

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableOverScroll()
    }

    private fun disableOverScroll() {
        listView.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        initTestConnectionButton()
    }

    private fun initTestConnectionButton() {
        val handler = CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                activity?.showToast("ERROR: ${throwable.message ?: throwable.toString()}")
            }
        }

        findPreference<Preference>(
            context?.getString(R.string.settings_test_connection) ?: return
        )?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                GlobalScope.launch(handler) {
                    val message = AnalyzeRepo(WeakReference(context)).testConnection()
                    lifecycleScope.launch {
                        activity?.showToast(message)
                    }
                }
                true
            }

        findPreference<SwitchPreference>(
            context?.getString(R.string.settings_auto_detect) ?: return
        )?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {

                true
            }
    }

    companion object {
        const val TAG = "SettingsPreferenceFragment"

        fun newInstance() = SettingsPreferenceFragment()
    }
}
