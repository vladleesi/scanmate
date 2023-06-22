package dev.vladleesi.scanmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vladleesi.scanmate.utils.Notifier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
