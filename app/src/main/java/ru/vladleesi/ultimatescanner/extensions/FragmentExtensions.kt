package ru.vladleesi.ultimatescanner.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showToast(
    text: String?,
    long: Boolean = false
) = Toast.makeText(context, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
