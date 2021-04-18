package ru.vladleesi.ultimatescanner.model

sealed class AnalyzeState {

    data class Error(val t: Throwable?) : AnalyzeState()

    object Success : AnalyzeState()

    object Loading : AnalyzeState()
}
