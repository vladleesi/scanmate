package ru.vladleesi.ultimatescanner.ui.fragments

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    protected inline fun <reified T : FragmentActivity> parent() = activity as? T
}
