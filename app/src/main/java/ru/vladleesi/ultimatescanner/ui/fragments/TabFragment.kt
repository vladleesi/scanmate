package ru.vladleesi.ultimatescanner.ui.fragments

import androidx.annotation.LayoutRes

abstract class TabFragment : BaseFragment {
    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)
}
