package ru.vladleesi.ultimatescanner.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.vladleesi.ultimatescanner.ui.activity.ResourceHolder
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraTabFragment
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.HistoryTabFragment
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.SettingsTabFragment

class MainTabAdapter(fm: FragmentManager, private val resourceHolder: ResourceHolder) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = listOf(
        CameraTabFragment.newInstance(),
        HistoryTabFragment.newInstance(),
        SettingsTabFragment.newInstance()
    )

    override fun getCount(): Int = fragments.size

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getPageTitle(position: Int): CharSequence =
        resourceHolder.getStringRes(fragments[position].pageTitleId)
}