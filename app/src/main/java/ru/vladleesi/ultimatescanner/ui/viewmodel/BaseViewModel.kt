package ru.vladleesi.ultimatescanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.utils.Notifier
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var notifier: Notifier

    private val coroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                notifier.showNotify(throwable.message ?: throwable.toString())
            }
        }
    }

    fun invoke(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            action()
        }
    }
}
