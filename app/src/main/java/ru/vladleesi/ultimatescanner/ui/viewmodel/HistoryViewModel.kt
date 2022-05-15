package ru.vladleesi.ultimatescanner.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.vladleesi.ultimatescanner.data.local.entity.HistoryEntity
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repo: AnalyzeRepo) : BaseViewModel() {

    private val _clearLiveData = MutableLiveData<Boolean>()
    val clearLiveData: LiveData<Boolean> = _clearLiveData

    private val _historyLiveData = MutableLiveData<List<HistoryEntity>>()
    val historyLiveData: LiveData<List<HistoryEntity>> = _historyLiveData

    fun clearHistory() {
        invoke {
            repo.clearHistory()
            _clearLiveData.postValue(true)
        }
    }

    fun loadHistory() {
        invoke {
            repo.getHistory()?.let(_historyLiveData::postValue)
        }
    }
}
