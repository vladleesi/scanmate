package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment

object TabFragmentFactory {

    fun create(position: Int): TabFragment = when (TabFragments.values()[position]) {
        TabFragments.CAMERA -> CameraTabFragment.newInstance()
        TabFragments.HISTORY -> HistoryTabFragment.newInstance()
        TabFragments.SETTINGS -> SettingsTabFragment.newInstance()
    }

    fun getTitle(position: Int) = TabFragments.values()[position].titleResId

    fun getSize() = TabFragments.values().size
}
