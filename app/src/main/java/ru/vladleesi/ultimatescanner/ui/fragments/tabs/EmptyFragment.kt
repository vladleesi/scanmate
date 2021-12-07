package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import androidx.fragment.app.Fragment
import ru.vladleesi.ultimatescanner.R

class EmptyFragment : Fragment(R.layout.fragment_empty) {

    companion object {
        const val TAG = "EmptyFragment"

        fun newInstance() = EmptyFragment()
    }
}
