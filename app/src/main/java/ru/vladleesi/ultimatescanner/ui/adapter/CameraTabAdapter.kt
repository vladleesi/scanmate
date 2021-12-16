package ru.vladleesi.ultimatescanner.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.vladleesi.ultimatescanner.ui.fragments.tabs.EmptyFragment

class CameraTabAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = listOf(
        EmptyFragment.newInstance(),
        EmptyFragment.newInstance()
    )

    private val titleList = listOf("Штрихкод", "Ручной режим")

    override fun getCount(): Int = titleList.size

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getPageTitle(position: Int): CharSequence = titleList[position]
}
