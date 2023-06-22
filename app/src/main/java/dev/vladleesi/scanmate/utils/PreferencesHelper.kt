package dev.vladleesi.scanmate.utils

import android.content.Context
import androidx.preference.PreferenceManager
import dev.vladleesi.preferences.get
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.data.repository.AnalyzeRepo
import java.lang.ref.WeakReference

class PreferencesHelper(private val contextWeakReference: WeakReference<Context>) {

    private val defaultPreferences =
        contextWeakReference.get()?.let { PreferenceManager.getDefaultSharedPreferences(it) }

    fun baseUrl() =
        defaultPreferences?.get(
            contextWeakReference.get()?.getString(R.string.settings_url).orEmpty(),
            AnalyzeRepo.DEFAULT_BASE_URL
        )?.removeSuffix("/")

    fun endpoint(): String {
        var endpoint = defaultPreferences?.get(
            contextWeakReference.get()?.getString(R.string.settings_endpoint).orEmpty(),
            AnalyzeRepo.DEFAULT_UPLOAD_ENDPOINT
        ) ?: AnalyzeRepo.DEFAULT_UPLOAD_ENDPOINT

        if (!endpoint.startsWith("/")) {
            endpoint = "/$endpoint"
        }

        return endpoint
    }
}
