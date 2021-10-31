package ru.vladleesi.ultimatescanner.ui.fragments

import android.os.Bundle
import androidx.fragment.app.FragmentFactory

object FragmentFactory {
    inline fun <reified T : BaseFragment> create(
        clazz: Class<T>,
        fillBundle: (Bundle) -> Unit
    ): T? {
        val classLoader = clazz.classLoader ?: return null
        val fragment =
            FragmentFactory.loadFragmentClass(classLoader, clazz.name).newInstance() as? T
        return fragment?.apply {
            arguments = Bundle().apply {
                fillBundle(this)
            }
        }
    }
}