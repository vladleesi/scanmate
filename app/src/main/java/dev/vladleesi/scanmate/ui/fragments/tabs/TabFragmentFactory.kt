package dev.vladleesi.scanmate.ui.fragments.tabs

import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.ui.fragments.TabFragment

object TabFragmentFactory {

    fun create(position: Int): TabFragment = when (TabFragments.values()[position]) {
        TabFragments.CAMERA -> CameraTabFragment.newInstance()
        TabFragments.HISTORY -> HistoryTabFragment.newInstance()
        TabFragments.SETTINGS -> SettingsTabFragment.newInstance()
    }

    fun getTitle(position: Int) = TabFragments.values()[position].titleResId

    fun getTabTitle(position: Int) = when (position) {
        0 -> R.string.page_title_auto_detect
        1 -> R.string.page_title_manual
        2 -> R.string.page_title_history
        3 -> R.string.page_title_settings
        else -> R.string.page_title_unknown
    }

    fun getSize() = TabFragments.values().size
}
