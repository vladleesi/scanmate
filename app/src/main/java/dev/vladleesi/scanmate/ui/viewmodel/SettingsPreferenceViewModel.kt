package dev.vladleesi.scanmate.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vladleesi.scanmate.data.repository.AnalyzeRepo
import javax.inject.Inject

@HiltViewModel
class SettingsPreferenceViewModel @Inject constructor(private val analyzeRepo: AnalyzeRepo) :
    BaseViewModel() {

    fun testConnection() {
        invoke {
            notifier.showNotify(analyzeRepo.testConnection())
        }
    }
}
