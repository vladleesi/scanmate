package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object VoiceEventBus {

    private val mText = MutableStateFlow("")

    val flow = mText.asStateFlow()

    fun toVoice(text: String) {
        mText.value = text
    }
}
