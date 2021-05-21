package ru.vladleesi.ultimatescanner.ui.model.state

sealed class Result<out T : Any> {

    data class Error(val t: Throwable?) : Result<Nothing>()

    data class Success<out T : Any>(val data: T?) : Result<T>()

    object Loading : Result<Nothing>()
}
