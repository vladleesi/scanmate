package dev.vladleesi.scanmate.ui.fragments.tabs

import androidx.fragment.app.Fragment
import dev.vladleesi.scanmate.R

class EmptyFragment : Fragment(R.layout.fragment_empty) {

    companion object {
        const val TAG = "EmptyFragment"

        fun newInstance() = EmptyFragment()
    }
}
