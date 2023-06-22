package dev.vladleesi.scanmate.utils

import android.content.Context
import dev.vladleesi.scanmate.extensions.showToast
import java.lang.ref.WeakReference

class Notifier(private val weakContext: WeakReference<Context>) {
    fun showNotify(text: String?) {
        weakContext.get()?.showToast(text = text, long = false)
    }
}
