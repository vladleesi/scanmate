package ru.vladleesi.ultimatescanner.utils

import android.content.Context
import ru.vladleesi.ultimatescanner.extensions.showToast
import java.lang.ref.WeakReference

class Notifier(private val weakContext: WeakReference<Context>) {
    fun showNotify(text: String?) {
        weakContext.get()?.showToast(text = text, long = false)
    }
}
