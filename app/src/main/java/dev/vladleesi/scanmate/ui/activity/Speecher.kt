package dev.vladleesi.scanmate.ui.activity

interface Speecher {
    fun voice(text: String?)
    fun stop()
}
