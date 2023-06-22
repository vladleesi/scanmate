package dev.vladleesi.scanmate.ui.activity

import androidx.annotation.StringRes

interface ResourceHolder {
    fun getStringRes(@StringRes stringResId: Int): String
}
