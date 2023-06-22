package dev.vladleesi.scanmate.ui.fragments

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

abstract class BaseFragment : Fragment {

    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    protected inline fun <reified T : FragmentActivity> parent() = activity as? T

    fun <T> LiveData<T>.observe(observer: Observer<T>) {
        observe(viewLifecycleOwner, observer)
    }
}
