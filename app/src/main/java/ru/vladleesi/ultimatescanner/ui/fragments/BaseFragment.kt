package ru.vladleesi.ultimatescanner.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class BaseFragment : Fragment() {

    protected inline fun <reified T : FragmentActivity> parent() = activity as? T
}
