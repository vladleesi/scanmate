package ru.vladleesi.ultimatescanner.ui.fragments

import androidx.annotation.LayoutRes

abstract class TabFragment : BaseFragment {
    abstract val pageTitleId: Int

    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)
}
