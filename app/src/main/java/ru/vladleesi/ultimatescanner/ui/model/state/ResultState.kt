package ru.vladleesi.ultimatescanner.ui.model.state

sealed class ResultState<out T : Any> {

    data class Error(val t: Throwable?) : ResultState<Nothing>()

    data class Success<out T : Any>(val data: T?) : ResultState<T>()

    object Loading : ResultState<Nothing>()
}
