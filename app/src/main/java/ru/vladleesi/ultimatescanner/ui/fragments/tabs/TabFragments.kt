package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import androidx.annotation.StringRes
import ru.vladleesi.ultimatescanner.R

enum class TabFragments(@StringRes val titleResId: Int) {
    CAMERA(R.string.page_title_camera),
    HISTORY(R.string.page_title_history),
    SETTINGS(R.string.page_title_settings)
}
