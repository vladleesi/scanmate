package ru.vladleesi.ultimatescanner.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.vladleesi.ultimatescanner.ui.activity.ResourceHolder
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.TabFragmentFactory
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.TabFragments

class InfinityMainTabAdapter(fm: FragmentManager, private val resourceHolder: ResourceHolder) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val realCount: Int = TabFragments.values().size

    val middle: Int
        get() {
            return if (TabFragments.values().size >= MIN_COUNT_FOR_INFINITY) {
                TabFragments.values().size * LOOP_COUNT / MULTIPLICITY
            } else {
                DEFAULT_MIDDLE
            }
        }

    fun getRealCount(): Int = realCount

    override fun getCount(): Int {
        return if (TabFragments.values().size >= MIN_COUNT_FOR_INFINITY) {
            realCount * LOOP_COUNT
        } else {
            realCount
        }
    }

    fun getRealPosition(position: Int) = position % realCount

    override fun getItem(position: Int): Fragment =
        TabFragmentFactory.create(getRealPosition(position))

    override fun getPageTitle(position: Int): CharSequence =
        resourceHolder.getStringRes(TabFragmentFactory.getTitle(getRealPosition(position)))

    private companion object {
        private const val DEFAULT_MIDDLE: Int = 0
        private const val MULTIPLICITY: Int = 2
        private const val MIN_COUNT_FOR_INFINITY: Int = 2
        private const val LOOP_COUNT: Int = 1000
    }
}
