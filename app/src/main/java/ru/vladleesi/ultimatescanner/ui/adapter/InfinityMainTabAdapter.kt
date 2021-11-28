package ru.vladleesi.ultimatescanner.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.vladleesi.ultimatescanner.ui.activity.ResourceHolder
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.CameraTabFragment
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.HistoryTabFragment
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.SettingsTabFragment

class InfinityMainTabAdapter(fm: FragmentManager, private val resourceHolder: ResourceHolder) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = listOf(
        HistoryTabFragment.newInstance(),
        CameraTabFragment.newInstance(autoDetect = true),
        SettingsTabFragment.newInstance(),
        CameraTabFragment.newInstance(autoDetect = false)
    )

    private val realCount: Int = fragments.size

    val middle: Int
        get() {
            return if (fragments.size >= MIN_COUNT_FOR_INFINITY) {
                fragments.size * LOOP_COUNT / MULTIPLICITY
            } else {
                DEFAULT_MIDDLE
            }
        }

    fun getRealCount(): Int = realCount

    override fun getCount(): Int {
        return if (fragments.size >= MIN_COUNT_FOR_INFINITY) {
            realCount * LOOP_COUNT
        } else {
            realCount
        }
    }

    fun getRealPosition(position: Int) = position % realCount

    override fun getItem(position: Int): Fragment = fragments[getRealPosition(position)]

    override fun getPageTitle(position: Int): CharSequence =
        resourceHolder.getStringRes(fragments[position].pageTitleId)

    private companion object {
        private const val DEFAULT_MIDDLE: Int = 0
        private const val MULTIPLICITY: Int = 2
        private const val MIN_COUNT_FOR_INFINITY: Int = 2
        private const val LOOP_COUNT: Int = 1000
    }
}
