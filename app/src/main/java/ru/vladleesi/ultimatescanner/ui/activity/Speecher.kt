package ru.vladleesi.ultimatescanner.ui.activity

interface Speecher {
    fun voice(text: String?)
    fun stop()
}
