package ru.vladleesi.ultimatescanner.ui.model

sealed class AnalyzeState {

    data class Error(val t: Throwable?) : AnalyzeState()

    data class Success(val data: String?) : AnalyzeState()

    object Loading : AnalyzeState()
}
