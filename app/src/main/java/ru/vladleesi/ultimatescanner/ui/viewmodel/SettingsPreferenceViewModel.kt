package ru.vladleesi.ultimatescanner.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
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
