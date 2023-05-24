package ru.vladleesi.ultimatescanner.utils

import android.content.Context
import androidx.preference.PreferenceManager
import ru.vladleesi.preferences_ktx.get
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import java.lang.ref.WeakReference

class PreferencesHelper(private val contextWeakReference: WeakReference<Context>) {

    private val defaultPreferences =
        contextWeakReference.get()?.let { PreferenceManager.getDefaultSharedPreferences(it) }

    fun baseUrl() =
        defaultPreferences?.get(
            contextWeakReference.get()?.getString(R.string.settings_url),
            AnalyzeRepo.DEFAULT_BASE_URL
        )?.removeSuffix("/")

    fun endpoint(): String {
        var endpoint = defaultPreferences?.get(
            contextWeakReference.get()?.getString(R.string.settings_endpoint),
            AnalyzeRepo.DEFAULT_UPLOAD_ENDPOINT
        ) ?: AnalyzeRepo.DEFAULT_UPLOAD_ENDPOINT

        if (!endpoint.startsWith("/")) {
            endpoint = "/$endpoint"
        }

        return endpoint
    }
}
