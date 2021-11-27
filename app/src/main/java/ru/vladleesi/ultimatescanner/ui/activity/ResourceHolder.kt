package ru.vladleesi.ultimatescanner.ui.activity

import androidx.annotation.StringRes

interface ResourceHolder {
    fun getStringRes(@StringRes stringResId: Int): String
}
