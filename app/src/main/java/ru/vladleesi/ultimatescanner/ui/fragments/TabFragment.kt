package ru.vladleesi.ultimatescanner.ui.fragments

import androidx.annotation.LayoutRes

abstract class TabFragment(@LayoutRes layoutId: Int) : BaseFragment(layoutId) {
    abstract val pageTitleId: Int
}
